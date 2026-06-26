package com.group7.olineordermeal.controller.admin;

import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员商家管理（封禁/解封/审核）
 * 访问路径：/admin/merchant/**   （完整例子：/admin/merchant/list）
 */
@Controller
@RequestMapping("/admin/merchant")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 商家列表（展示所有商家）
     */
    @GetMapping("/list")
    public String list(Model model) {
        // 调用 Service 查询全部商家
        List<Merchant> merchants = merchantService.listAll();
        model.addAttribute("merchants", merchants);
        // 返回视图：templates/admin/merchant_list.html
        return "admin/merchant_list";
    }

    /**
     * 封禁商家（将 status 设为 BANNED）
     */
    @GetMapping("/ban/{id}")
    public String ban(@PathVariable Long id) {
        merchantService.updateStatus(id, "BANNED");
        return "redirect:/admin/merchant/list";
    }

    /**
     * 解封商家（将 status 设为 APPROVED）
     */
    @GetMapping("/unban/{id}")
    public String unban(@PathVariable Long id) {
        merchantService.updateStatus(id, "APPROVED");
        return "redirect:/admin/merchant/list";
    }

    /**
     * 审核通过（将 status 设为 APPROVED）
     */
    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        merchantService.updateStatus(id, "APPROVED");
        return "redirect:/admin/merchant/list";
    }
}
