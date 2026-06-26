package com.group7.olineordermeal.controller.merchant;

import com.group7.olineordermeal.dto.CategoryDTO;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.service.DishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/category")
public class MerchantCategoryController {

    @Autowired
    private DishCategoryService categoryService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("categories", categoryService.listByMerchant(merchant.getId()));
        return "merchant/category_list";
    }

    @PostMapping("/add")
    public String add(CategoryDTO dto, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        categoryService.addCategory(merchant.getId(), dto);
        return "redirect:/merchant/category";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, CategoryDTO dto, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        categoryService.updateCategory(id, dto);
        return "redirect:/merchant/category";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        categoryService.deleteCategory(id, merchant.getId());
        return "redirect:/merchant/category";
    }
}