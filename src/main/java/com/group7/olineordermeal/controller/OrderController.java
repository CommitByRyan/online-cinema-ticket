package com.group7.olineordermeal.controller;

import com.group7.olineordermeal.dto.CartItemDTO;
import com.group7.olineordermeal.dto.CreateOrderDTO;
import com.group7.olineordermeal.entity.Address;
import com.group7.olineordermeal.entity.Order;
import com.group7.olineordermeal.entity.OrderItem;
import com.group7.olineordermeal.entity.User;
import com.group7.olineordermeal.service.AddressService;
import com.group7.olineordermeal.service.CartService;
import com.group7.olineordermeal.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private CartService cartService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private OrderService orderService;

    /**
     * 订单确认页：展示购物车商品、地址列表、总金额
     */
    @GetMapping("/confirm")
    public String confirmPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 购物车商品
        List<CartItemDTO> cartItems = cartService.getCartItems(user.getId());
        if (cartItems.isEmpty()) {
            return "redirect:/cart";  // 购物车为空，跳回购物车页
        }
        BigDecimal total = cartItems.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 用户地址列表
        List<Address> addresses = addressService.listByUser(user.getId());

        // ★ 这里使用 lambda（方案 2.1）或 Address::isDefault（方案 2.2） ★
        Address defaultAddr = addresses.stream()
                .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault() == 1)
                .findFirst()
                .orElse(null);
        // 如果已经在 Address 中写了 isDefault():
        // Address defaultAddr = addresses.stream()
        //         .filter(Address::isDefault)
        //         .findFirst()
        //         .orElse(null);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddr);
        return "confirm_order";
    }

    /**
     * 创建订单（提交确认页表单）
     */
    @PostMapping("/create")
    public String createOrder(CreateOrderDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.createOrder(user.getId(), dto);
        return "redirect:/order/pay?orderId=" + order.getId();
    }

    /**
     * 支付入口页：显示订单金额、剩余支付时间，提供模拟支付按钮
     */
    @GetMapping("/pay")
    public String payPage(@RequestParam Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            return "redirect:/user/orders";
        }
        model.addAttribute("order", order);
        return "pay_entry";
    }

    /**
     * 模拟支付请求
     */
    @PostMapping("/doPay")
    public String doPay(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            orderService.payOrder(orderId);
            return "redirect:/order/success?orderId=" + orderId;
        } catch (Exception e) {
            return "redirect:/order/pay?orderId=" + orderId + "&error=" + e.getMessage();
        }
    }

    /**
     * 支付成功页
     */
    @GetMapping("/success")
    public String paySuccess(@RequestParam Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "pay_success";
    }

    /**
     * 用户取消订单（仅待支付）
     */
    @GetMapping("/cancel")
    public String cancelOrder(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            orderService.cancelOrder(orderId);
        } catch (Exception e) {
            // 可记录日志
        }
        return "redirect:/user/orders";
    }

    /**
     * 订单详情页
     */
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            return "redirect:/user/orders";
        }
        List<OrderItem> items = orderService.getOrderItems(orderId);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "order_detail";
    }
}