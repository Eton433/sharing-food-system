package com.example.sharingfood.controller;

import com.example.sharingfood.model.Customer;
import com.example.sharingfood.repository.CustomerRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final CustomerRepository customerRepository;

    public HomeController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * 確保用戶已登入的方法
     */
    private Customer ensureLoggedIn(HttpSession session) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            throw new IllegalStateException("User not logged in.");
        }
        return loggedInCustomer;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // 如果用戶已登入，直接跳轉到首頁
        if (session.getAttribute("loggedInCustomer") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        if (!email.endsWith("@nccu.edu.tw")) {
            model.addAttribute("error", "Only NCCU students can log in.");
            return "login";
        }

        Customer customer = customerRepository.findByEmail(email);
        if (customer == null || !customer.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }

        session.setAttribute("loggedInCustomer", customer);
        return "redirect:/";
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        try {
            Customer loggedInCustomer = ensureLoggedIn(session);
            model.addAttribute("loggedInCustomer", loggedInCustomer);
            return "index";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 清除所有會話屬性
        return "redirect:/login";
    }
}
