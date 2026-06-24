package com.group7.olineordermeal.controller;

import com.group7.olineordermeal.common.result.Result;
import com.group7.olineordermeal.dto.RegisterDTO;
import com.group7.olineordermeal.entity.User;
import com.group7.olineordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

    @Controller
    public class AuthController {

        @Autowired
        private UserService userService;

        // 显示登录页
        @GetMapping("/login")
        public String loginPage() {
            return "login";
        }

        // 处理登录请求（表单提交）
        @PostMapping("/login")
        public String login(@RequestParam String loginName,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
            try {
                User user = userService.login(loginName, password);
                if (user == null) {
                    // 用户名不存在，跳转到注册页
                    return "redirect:/register";
                }
                session.setAttribute("user", user);
                return "redirect:/dish/list";  // 跳转到菜品浏览页（组员B负责）
            } catch (Exception e) {
                // 密码错误或其他异常
                model.addAttribute("error", e.getMessage());
                model.addAttribute("showFindPwd", true);  // 显示找回密码按钮
                return "login";
            }
        }

        // 显示注册页
        @GetMapping("/register")
        public String registerPage() {
            return "register";
        }

        // 处理注册请求
        @PostMapping("/register")
        public String register(RegisterDTO dto, Model model) {
            try {
                userService.register(dto);
                return "redirect:/login";  // 注册成功跳转到登录页
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                return "register";
            }
        }

        // 退出登录
        @GetMapping("/logout")
        public String logout(HttpSession session) {
            session.invalidate();
            return "redirect:/login";
        }
    }

