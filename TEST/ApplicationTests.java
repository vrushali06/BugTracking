package com.hsbc.application;

import com.hsbc.application.Dao.UserDaoImpl;
import com.hsbc.application.controller.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {
	public static void main(String[] args) {
		UserDaoImpl userDao = new UserDaoImpl();
		AuthService authService = new AuthService(userDao);

		// Simulate login and attempt to access a restricted area
		String result = authService.loginAndAccessRestrictedArea("adminUser", "adminPassword");
		System.out.println(result);
	}

	@Test
	void contextLoads() {
	}

}
