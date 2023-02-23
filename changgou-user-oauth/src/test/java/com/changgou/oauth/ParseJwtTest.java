package com.changgou.oauth;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    @Test
    public void parseJwt(){
        //基于公钥去解析jwt
        String jwt ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4iLCJuYW1lIjoiZ2piIn0.Aj7DIBLQCWrFTI2UymevucfYMCAHjxJfRqd1NdxdTzMEbaGk9bAtpGsa3EWnafqovxRYu_0HjWfU8bRmENCGSh9z8-WEaTS6JHF0wtRIPhIvimujpJCSBdCpIW0ahKtx5WojLC5C0b2EWvTKBDs8-7Ybkfs7UE9CdAJdTvGUr5Y2QIiVn1T0FfdYPR6zOU_QpTyo5l-VEMldk64VDFeiZy9tg0dq-3M_Yoopvx86SGDzKPUQ36oI9yiamsNQcwnhOgOu7IchfL4rM4ku9X2O6j-BvM0qxr1iSu_WjFPVBug3mY134IArBGFK7XKVqt5SelEvTO6AfFEJ2vZ5oTqlCg";
        String publicKey ="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";

        Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publicKey));

        String claims = token.getClaims();
        System.out.println(claims);
    }
}
