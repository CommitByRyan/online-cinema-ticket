package com.group7.olineordermeal.controller.admin;

import com.github.pagehelper.PageInfo;
import com.group7.olineordermeal.dto.DishSearchDTO;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class    DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品列表页（支持搜索）
     */
    @GetMapping("/dish/list")
    public String dishList(DishSearchDTO search,
                           @RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "8") int pageSize,
                           Model model) {
        PageInfo<Dish> pageInfo = dishService.searchDishPage(search, pageNum, pageSize);
        model.addAttribute("page", pageInfo);   // <---- 必须是 "page"
        model.addAttribute("search", search);
        return "dish_list";
    }


    /**
     * 菜品详情（可选，用于点击详情按钮）
     */
    @GetMapping("/dish/detail")
    public String dishDetail(@RequestParam Long id, Model model) {
        Dish dish = dishService.getDishDetail(id);
        model.addAttribute("dish", dish);
        return "dish_detail";
    }
}