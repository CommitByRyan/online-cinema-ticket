package com.group7.olineordermeal.controller;

import com.group7.olineordermeal.dto.AddressDTO;
import com.group7.olineordermeal.dto.PasswordUpdateDTO;
import com.group7.olineordermeal.entity.Address;
import com.group7.olineordermeal.entity.User;
import com.group7.olineordermeal.service.AddressService;
import com.group7.olineordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;

/**
 * 统一管理用户中心所有页面：
 *  - 个人信息（profile） & 密码/手机号修改
 *  - 收货地址 CRUD
 *  - 我的订单（占位）
 *
 * 关键点：
 * 1️⃣ 每一个 URL 只出现一次映射，避免 Spring 启动时的 “Ambiguous mapping” 错误。
 * 2️⃣ 新增/编辑地址均使用同一个表单页面 `address_form.html`，通过模型属性区分（add 用 addressDTO，edit 用 address）。
 * 3️⃣ 所有需要登录的接口都先从 session 中取出 `User`，若为空直接重定向登录页。
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    // -----------------------------------------------------------------
    // 1️⃣ 个人中心
    // -----------------------------------------------------------------
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "profile";
    }

    // ------------------- 修改密码 -------------------
    @PostMapping("/updatePassword")
    public String updatePassword(@Valid PasswordUpdateDTO dto,
                                 HttpSession session,
                                 Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            userService.updatePassword(user.getId(),
                    dto.getOldPassword(),
                    dto.getNewPassword());
            model.addAttribute("success", "密码修改成功，请重新登录");
            // 强制退出，防止旧密码继续使用
            session.invalidate();
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }

    // ------------------- 修改手机号 -------------------
    @PostMapping("/updatePhone")
    public String updatePhone(@RequestParam String newPhone,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            userService.updatePhone(user.getId(), newPhone);
            // 同步到 session 中
            user.setPhone(newPhone);
            session.setAttribute("user", user);
            model.addAttribute("success", "手机号修改成功");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "profile";
    }

    // -----------------------------------------------------------------
    // 2️⃣ 收货地址管理（列表 / 新增 / 编辑 / 删除 / 设为默认）
    // -----------------------------------------------------------------
    @GetMapping("/address")
    public String addressList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("addressList", addressService.listByUser(user.getId()));
        return "address_list";
    }

    // ------------------- 新增地址 – 表单 -------------------
    @GetMapping("/address/add")
    public String addAddressForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 传一个空的 DTO，页面会通过 th:field 绑定属性
        model.addAttribute("addressDTO", new AddressDTO());
        return "address_form";
    }

    // ------------------- 新增地址 – 处理提交 -------------------
    @PostMapping("/address/add")
    public String addAddress(@Valid @ModelAttribute("addressDTO") AddressDTO addressDTO,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 校验不通过，返回表单并显示错误信息
        if (bindingResult.hasErrors()) {
            return "address_form";
        }

        try {
            addressService.addAddress(user.getId(), addressDTO);
        } catch (Exception e) {
            // 业务层抛出的异常（如唯一约束、SQL 错误）在页面顶部统一展示
            model.addAttribute("errorMsg", e.getMessage());
            return "address_form";
        }

        // 成功后 PRG（Post‑Redirect‑Get），防止刷新重复提交
        return "redirect:/user/address";
    }

    // ------------------- 编辑地址 – 表单 -------------------
    @GetMapping("/address/edit/{id}")
    public String editAddressForm(@PathVariable Long id,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 只允许编辑自己的地址
        Address address = addressService.listByUser(user.getId())
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (address == null) {
            // 找不到或不属于当前用户 → 重定向回列表
            return "redirect:/user/address";
        }

        // 把查询到的实体直接放进模型，页面通过 th:object="address" 绑定
        model.addAttribute("address", address);
        return "address_form";   // 复用同一个模板
    }

    // ------------------- 编辑地址 – 处理提交 -------------------
    @PostMapping("/address/edit/{id}")
    public String editAddress(@PathVariable Long id,
                              @Valid @ModelAttribute("address") AddressDTO addressDTO,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            // 为了让页面仍能显示原来的 address.id，需要把 id 再放回模型
            model.addAttribute("address", addressDTO);
            return "address_form";
        }

        try {
            addressService.updateAddress(id, addressDTO);
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            // 为了页面仍能渲染 address.id，这里把 id 再放进去
            model.addAttribute("address", addressDTO);
            return "address_form";
        }

        return "redirect:/user/address";
    }

    // ------------------- 删除地址 -------------------
    @GetMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            addressService.deleteAddress(id, user.getId());
        }
        return "redirect:/user/address";
    }

    // ------------------- 设为默认地址 -------------------
    @GetMapping("/address/setDefault/{id}")
    public String setDefaultAddress(@PathVariable Long id,
                                    HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            addressService.setDefault(id, user.getId());
        }
        return "redirect:/user/address";
    }

    // -----------------------------------------------------------------
    // 3️⃣ 我的订单（占位，稍后由组员 D/E 完成）
    // -----------------------------------------------------------------
    @GetMapping("/orders")
    public String myOrders(@RequestParam(required = false) String status,
                           HttpSession session,
                           Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 暂时返回空列表，页面会显示 “暂无订单”
        model.addAttribute("orders", Collections.emptyList());
        model.addAttribute("currentStatus", status);
        return "my_orders";
    }
}
