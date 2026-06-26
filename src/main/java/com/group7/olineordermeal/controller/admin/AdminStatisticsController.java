package com.group7.olineordermeal.controller.admin;



import com.group7.olineordermeal.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminStatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/statistics")
    public String statistics(Model model) {
        long totalOrders = statisticsService.getTotalOrderCount();
        BigDecimal totalRevenue = statisticsService.getTotalRevenue();
        long activeUsers = statisticsService.getActiveUserCount();
        long activeMerchants = statisticsService.getActiveMerchantCount();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("activeMerchants", activeMerchants);
        return "admin/platform_statistics";
    }

}