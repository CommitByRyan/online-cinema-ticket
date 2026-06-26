package com.group7.olineordermeal.controller.merchant;



import com.group7.olineordermeal.dto.OrderStatusUpdateDTO;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.entity.Order;
import com.group7.olineordermeal.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/merchant/order")
public class MerchantOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) String status,
                       HttpSession session,
                       Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        List<Order> orders = orderService.getOrdersByMerchant(merchant.getId(), status);
        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
        return "merchant/order_list";
    }

    @PostMapping("/updateStatus")
    public String updateStatus(OrderStatusUpdateDTO dto,
                               HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        try {
            orderService.updateOrderStatus(dto.getOrderId(), dto.getTargetStatus(), merchant.getId());
        } catch (Exception e) {
            // 可记录日志，或者重定向带错误参数
        }
        return "redirect:/merchant/order/list";
    }
}