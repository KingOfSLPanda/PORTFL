package com.portfl.web.controller;

import com.portfl.listeners.OnRegistrationCompleteEvent;
import com.portfl.model.User;
import com.portfl.service.SecurityService;
import com.portfl.service.UserService;
import com.portfl.utils.UrlUtils;
import com.portfl.web.dto.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;

import static com.portfl.constants.Constants.Redirect.TO_HOME;
import static com.portfl.constants.Constants.Views.LOGIN_PAGE;
import static com.portfl.constants.Constants.Views.REGISTRATION_PAGE;

/**
 * Created by Vlad on 22.03.17.
 */

@Controller
public class AuthController {

    @Autowired
    public ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(UserForm userForm) {
        SecurityContextHolder.clearContext();
        return LOGIN_PAGE;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(UserForm userForm) {
        return LOGIN_PAGE;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginSubmit(@Valid @ModelAttribute UserForm userForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return LOGIN_PAGE;
        }
        if (userService.isAccountEnabled(userForm.getUsername())) {
            securityService.autoLogin(userForm.getUsername(), userForm.getPassword());
            return TO_HOME;
        }
        model.addAttribute("enabled", true);
        return LOGIN_PAGE;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(UserForm userForm) {
        return REGISTRATION_PAGE;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registrationSubmit(@Valid @ModelAttribute UserForm userForm, BindingResult result, WebRequest request, Model model) {
        if (result.hasErrors()) {
            return REGISTRATION_PAGE;
        }
        String username = userForm.getUsername();
        String email = userForm.getEmail();
        if (isExistUsername(username)) {
            model.addAttribute("existUsername", true);
            return REGISTRATION_PAGE;
        }
        if (isExistEmail(email)) {
            model.addAttribute("existEmail", true);
            return REGISTRATION_PAGE;
        }
        User user = new User();
        user.setFirstName(userForm.getFirstName());
        user.setLastName(userForm.getLastName());
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(userForm.getPassword());
        user.setEnabled(false);
        userService.save(user);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), UrlUtils.getAppUrl(request)));
        return LOGIN_PAGE;
    }

    @RequestMapping(value = "/registrationConfirm.html")
    public String registrationConfirm(@RequestParam("token") String token, UserForm userForm) {
        if (userService.enableAccount(token)) {
            return LOGIN_PAGE;
        }
        return TO_HOME;
    }

    @RequestMapping(value = "/isExistUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    boolean isExistUsername(@RequestParam String username) {
        if (userService.isExistUsername(username)) {
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/isExistEmail", method = RequestMethod.GET)
    public
    @ResponseBody
    boolean isExistEmail(@RequestParam String email) {
        if (userService.isExistEmail(email)) {
            return true;
        }
        return false;
    }
}
