package com.group7.olineordermeal.controller.merchant;

import com.group7.olineordermeal.dto.MerchantRegisterDTO;
import com.group7.olineordermeal.entity.Merchant;
import com.group7.olineordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant")
public class MerchantAuthController {

    @Autowired
    private MerchantService merchantService;

    // ------------------- 登录页 -------------------
    @GetMapping("/login")
    public String loginPage() {
        return "merchant/merchant_login";      // <‑‑ 这里必须对应模板路径
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            Merchant merchant = merchantService.login(username, password);
            if (merchant == null) {
                // 商家未审核，通过后端返回 null，跳转到注册页
                return "redirect:/merchant/register";
            }
            session.setAttribute("merchant", merchant);
            // 登录成功后跳转到商家菜品管理页
            return "redirect:/merchant/home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "merchant/merchant_login";
        }
    }

    // ------------------- 注册页 -------------------
    @GetMapping("/register")
    public String registerPage() {
        return "merchant/merchant_register";
    }

    @PostMapping("/register")
    public String register(MerchantRegisterDTO dto, Model model) {
        try {
            merchantService.register(dto);
            // 注册成功 → 跳转到登录页并提示
            return "redirect:/merchant/login?msg=注册成功，请等待管理员审核";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "merchant/merchant_register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("merchant");
        return "redirect:/merchant/login";
    }
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) {
            // 未登录或 session 失效，直接回登录页
            return "redirect:/merchant/login";
        }
        // 把 merchant 对象放到模型，页面里可以直接使用 ${merchant}
        model.addAttribute("merchant", merchant);
        return "merchant/home";   // 对应 templates/merchant/home.html
    }

}