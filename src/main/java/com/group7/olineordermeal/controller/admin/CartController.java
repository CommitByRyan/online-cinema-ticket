package com.group7.olineordermeal.controller.admin;

import com.group7.olineordermeal.dto.CartItemDTO;
import com.group7.olineordermeal.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.group7.olineordermeal.service.CartService;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;



@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取当前登录用户
     */
    private User getLoginUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    /**
     * 购物车页面
     */
    @GetMapping
    public String cartPage(HttpSession session, Model model) {
        User user = getLoginUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItemDTO> items = cartService.getCartItems(user.getId());
        BigDecimal total = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, (sum, val) -> val == null ? sum : sum.add(val));
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long dishId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) return "redirect:/login";
        try {
            cartService.addToCart(user.getId(), dishId, quantity);
        } catch (Exception e) {
            return "redirect:/dish/list?error=" + e.getMessage();
        }
        return "redirect:/cart";
    }

    /**
     * 修改购物车数量
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long dishId,
                                 @RequestParam Integer quantity,
                                 HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) return "redirect:/login";
        try {
            cartService.updateQuantity(user.getId(), dishId, quantity);
        } catch (Exception e) {
            return "redirect:/cart?error=" + e.getMessage();
        }
        return "redirect:/cart";
    }

    /**
     * 删除单个购物车商品（POST 符合REST规范）
     */
    @PostMapping("/remove")
    public String remove(@RequestParam Long dishId, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) return "redirect:/login";
        cartService.removeFromCart(user.getId(), dishId);
        return "redirect:/cart";
    }

    /**
     * 清空购物车（POST）
     */
    @PostMapping("/clear")
    public String clear(HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) return "redirect:/login";
        cartService.clearCart(user.getId());
        return "redirect:/cart";
    }
}