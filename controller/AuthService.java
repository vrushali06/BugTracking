package com.hsbc.application.controller;

import com.hsbc.application.Dao.UserDaoImpl;

public class AuthService {

    private final UserDaoImpl userDao;

    public AuthService(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public String loginAndAccessRestrictedArea(String userName, String password) {
        // Step 1: User logs in
        String jwt = userDao.loginUser(userName, password);
        if (jwt != null) {
            // Step 2: Attempt to access a restricted endpoint using the JWT
            try {
                userDao.restrictedEndpoint(jwt); // Pass the JWT token
                return "Access granted!";
            } catch (RuntimeException e) {
                return "Access denied: " + e.getMessage();
            }
        }
        return "Login failed!";
    }

}
