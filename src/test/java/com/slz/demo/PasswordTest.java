package com.slz.demo;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码加密测试
 */
class PasswordTest {

    @Test
    void encrypt() {
        String rawPassword = "admin123";
        String encrypted = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        System.out.println("明文: " + rawPassword);
        System.out.println("密文: " + encrypted);
        System.out.println("验证: " + BCrypt.checkpw(rawPassword, encrypted));
    }
}
