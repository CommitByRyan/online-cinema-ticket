package com.group7.olineordermeal.controller.merchant;

import com.group7.olineordermeal.dto.MerchantUpdateDTO;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/info")
public class MerchantInfoController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping
    public String infoPage(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("merchant", merchant);
        return "merchant/merchant_info";
    }

    @PostMapping("/update")
    public String updateInfo(MerchantUpdateDTO dto, HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        try {
            merchantService.updateInfo(merchant.getId(), dto);
            // 更新 session 中的信息
            merchant.setMerchantName(dto.getMerchantName());
            merchant.setLegalPerson(dto.getLegalPerson());
            merchant.setPhone(dto.getPhone());
            merchant.setAddress(dto.getAddress());
            merchant.setBusinessLicense(dto.getBusinessLicense());
            merchant.setBusinessStatus(dto.getBusinessStatus());
            session.setAttribute("merchant", merchant);
            model.addAttribute("success", "更新成功");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("merchant", merchant);
        return "merchant/merchant_info";
    }
}