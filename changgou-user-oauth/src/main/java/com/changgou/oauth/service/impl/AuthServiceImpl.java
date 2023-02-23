package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
/****
 * 发送Http请求创建令牌
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${auth.ttl}")
    private long ttl;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //采用客户端负载均衡，从eureka获取认证服务的ip 和端口
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        URI uri = serviceInstance.getUri();
        //申请令牌地址
        String url=uri+"/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        //进行Base64编码,并将编码后的认证数据放到头文件中
        headers.add("Authorization",this.getHttpBasic(clientId,clientSecret));
        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(body,headers);

        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    //当响应的值为400或401时候也要正常响应，不要抛出异常
                    super.handleError(response);
                }
            }
        });

        //远程调用申请令牌
        System.out.println(url);
        // uri+"/oauth/token"用来进行访问 UserDetailsServiceImpl来进行获取
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = responseEntity.getBody();
        System.out.println(map);
        if (map == null || map.get("access_token") == null || map.get("refresh_token") == null || map.get("jti") == null){
            //申请令牌失败
            throw new RuntimeException("申请令牌失败");
        }

        //2.封装结果数据
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken((String) map.get("access_token"));
        authToken.setRefreshToken((String) map.get("refresh_token"));
        authToken.setJti((String)map.get("jti"));

        //3.将jti作为redis中的key,将jwt作为redis中的value进行数据的存放
        stringRedisTemplate.boundValueOps(authToken.getJti()).set(authToken.getAccessToken(),ttl, TimeUnit.SECONDS);
        return authToken;
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String value = clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(value.getBytes());
        return "Basic "+new String(encode);
    }
}
