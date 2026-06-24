package com.group7.olineordermeal.controller;

import com.group7.olineordermeal.common.result.Result;
import com.group7.olineordermeal.dto.AddressDTO;
import com.group7.olineordermeal.dto.PasswordUpdateDTO;
import com.group7.olineordermeal.entity.Address;
import com.group7.olineordermeal.entity.User;
import com.group7.olineordermeal.service.AddressService;
import com.group7.olineordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

    @Controller
    @RequestMapping("/user")
    public class UserController {

        @Autowired
        private UserService userService;

        @Autowired
        private AddressService addressService;

        // 个人中心首页（显示用户信息，修改密码/手机号的入口）
        @GetMapping("/profile")
        public String profile(HttpSession session, Model model) {
            User user = (User) session.getAttribute("user");
            if (user == null) return "redirect:/login";
            model.addAttribute("user", user);
            return "profile";
        }

        // 修改密码（表单提交）
        @PostMapping("/updatePassword")
        public String updatePassword(PasswordUpdateDTO dto, HttpSession session, Model model) {
            User user = (User) session.getAttribute("user");
            try {
                userService.updatePassword(user.getId(), dto.getOldPassword(), dto.getNewPassword());
                model.addAttribute("success", "密码修改成功，请重新登录");
                session.invalidate();  // 强制重新登录
                return "redirect:/login";
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                return "profile";
            }
        }

        // 修改手机号（简单模拟，无需验证码）
        @PostMapping("/updatePhone")
        public String updatePhone(@RequestParam String newPhone, HttpSession session, Model model) {
            User user = (User) session.getAttribute("user");
            try {
                userService.updatePhone(user.getId(), newPhone);
                user.setPhone(newPhone);  // 更新 session 中的信息
                session.setAttribute("user", user);
                model.addAttribute("success", "手机号修改成功");
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
            return "profile";
        }

        // ========== 地址管理 ==========
        @GetMapping("/address")
        public String addressList(HttpSession session, Model model) {
            User user = (User) session.getAttribute("user");
            model.addAttribute("addressList", addressService.listByUser(user.getId()));
            return "address_list";
        }

        @GetMapping("/address/add")
        public String addAddressForm() {
            return "address_form";
        }

        @PostMapping("/address/add")
        public String addAddress(AddressDTO dto, HttpSession session) {
            User user = (User) session.getAttribute("user");
            addressService.addAddress(user.getId(), dto);
            return "redirect:/user/address";
        }

        @GetMapping("/address/edit/{id}")
        public String editAddressForm(@PathVariable Long id, HttpSession session, Model model) {
            User user = (User) session.getAttribute("user");
            // 验证权限：只能编辑自己的地址
            Address address = addressService.listByUser(user.getId()).stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (address == null) return "redirect:/user/address";
            model.addAttribute("address", address);
            return "address_form";
        }

        @PostMapping("/address/update/{id}")
        public String updateAddress(@PathVariable Long id, AddressDTO dto, HttpSession session) {
            User user = (User) session.getAttribute("user");
            addressService.updateAddress(id, dto);
            return "redirect:/user/address";
        }

        @GetMapping("/address/delete/{id}")
        public String deleteAddress(@PathVariable Long id, HttpSession session) {
            User user = (User) session.getAttribute("user");
            addressService.deleteAddress(id, user.getId());
            return "redirect:/user/address";
        }

        @GetMapping("/address/setDefault/{id}")
        public String setDefaultAddress(@PathVariable Long id, HttpSession session) {
            User user = (User) session.getAttribute("user");
            addressService.setDefault(id, user.getId());
            return "redirect:/user/address";
        }

        // ========== 我的订单（需要调用组员D/E的订单服务） ==========
        // 注意：这里依赖 OrderService，需要等组员D/E完成后才能正常工作。
        // 在开发初期，你可以先写一个假的 OrderService 返回空列表，或者让组长提供一个接口。
        @GetMapping("/orders")
        public String myOrders(@RequestParam(required = false) String status,
                               HttpSession session,
                               Model model) {
            User user = (User) session.getAttribute("user");
            // TODO: 等组员D/E提供 OrderService 后，调用 service.listByUser(user.getId(), status)
            // 现在先 mock 一个空列表，让页面能显示“暂无订单”
            model.addAttribute("orders", java.util.Collections.emptyList());
            model.addAttribute("currentStatus", status);
            return "my_orders";
        }
    }

