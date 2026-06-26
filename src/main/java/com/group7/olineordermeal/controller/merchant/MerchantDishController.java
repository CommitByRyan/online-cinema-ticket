package com.group7.olineordermeal.controller.merchant;

import com.group7.olineordermeal.dto.DishDTO;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.service.DishCategoryService;
import com.group7.olineordermeal.service.MerchantDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 商家后台菜品管理（增删改查 + 上下架）
 */
@Controller
@RequestMapping("/merchant/dish")
public class MerchantDishController {

    @Autowired
    private MerchantDishService dishService;

    @Autowired
    private DishCategoryService categoryService;

    // ----------------- 菜品列表 -----------------
    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        model.addAttribute("dishes", dishService.listByMerchant(merchant.getId()));
        return "merchant/dish_list";
    }

    // ----------------- 添加页面 -----------------
    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        model.addAttribute("categories", categoryService.listByMerchant(merchant.getId()));
        return "merchant/dish_form";
    }

    // ----------------- 添加提交 -----------------
    @PostMapping("/add")
    public String add(@ModelAttribute DishDTO dto,
                      HttpSession session,
                      Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        dishService.addDish(merchant.getId(), dto);
        return "redirect:/merchant/dish/list";
    }

    // ----------------- 编辑页面 -----------------
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        model.addAttribute("dish", dishService.getDish(id));
        model.addAttribute("categories", categoryService.listByMerchant(merchant.getId()));
        return "merchant/dish_form";
    }

    // ----------------- 编辑提交 -----------------
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute DishDTO dto,
                         HttpSession session,
                         Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        dto.setId(id);
        dishService.updateDish(id, dto);
        return "redirect:/merchant/dish/list";
    }

    // ----------------- 删除 -----------------
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        dishService.deleteDish(id, merchant.getId());
        return "redirect:/merchant/dish/list";
    }

    // ----------------- 上下架（状态切换） -----------------
    @GetMapping("/toggleStatus/{id}")
    public String toggleStatus(@PathVariable Long id,
                              HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";

        // 必须先把 Dish 实体取出来，才能判断当前状态
        Dish dish = dishService.getDish(id);
        String newStatus = "ONLINE".equals(dish.getStatus()) ? "OFFLINE" : "ONLINE";

        // 调用 Service 完成状态更新（已经做了商家权限校验）
        dishService.updateStatus(id, newStatus, merchant.getId());

        return "redirect:/merchant/dish/list";
    }
}
