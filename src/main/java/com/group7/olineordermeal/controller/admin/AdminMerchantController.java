package com.group7.olineordermeal.controller.admin;

import com.group7.olineordermeal.common.result.Result;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/merchant")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    // 查看所有待审核的商家
    @GetMapping("/pending")
    public Result<List<Merchant>> pendingList() {
        List<Merchant> list = merchantService.listByStatus("PENDING");
        return Result.success(list);
    }

    // 通过审核（将状态改为 APPROVED）
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        merchantService.updateStatus(id, "APPROVED");
        return Result.success();
    }

    // 封禁商家
    @PostMapping("/ban/{id}")
    public Result<Void> ban(@PathVariable Long id) {
        merchantService.updateStatus(id, "BANNED");
        return Result.success();
    }

}