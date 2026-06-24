package com.group7.olineordermeal.controller.admin;

import com.group7.olineordermeal.dto.CartItemDTO;
import com.group7.olineordermeal.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 查看购物车页面
     */
    @GetMapping
    public String cartPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItemDTO> items = cartService.getCartItems(user.getId());
        BigDecimal total = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    /**
     * 添加菜品到购物车（AJAX 或 重定向）
     * 这里提供两种方式：前端用 form 或 异步。我们使用重定向，简单。
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long dishId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            cartService.addToCart(user.getId(), dishId, quantity);
        } catch (Exception e) {
            // 错误时可以在页面显示，这里简单重定向并带参数
            return "redirect:/dish/list?error=" + e.getMessage();
        }
        return "redirect:/cart";  // 添加后跳转到购物车页
    }

    /**
     * 更新购物车数量（AJAX 或 表单，这里用 POST 重定向）
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long dishId,
                                 @RequestParam Integer quantity,
                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        cartService.updateQuantity(user.getId(), dishId, quantity);
        return "redirect:/cart";
    }

    /**
     * 删除购物车菜品
     */
    @GetMapping("/remove")
    public String remove(@RequestParam Long dishId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        cartService.removeFromCart(user.getId(), dishId);
        return "redirect:/cart";
    }

    /**
     * 清空购物车
     */
    @GetMapping("/clear")
    public String clear(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        cartService.clearCart(user.getId());
        return "redirect:/cart";
    }
}
