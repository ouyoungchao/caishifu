package com.macro.mall.portal.service.impl;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.macro.mall.portal.service.MessageService;
import com.macro.mall.portal.service.UmsMemberCacheService;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class MessageServiceImpl implements MessageService {

    @Value("${sms.accessKey}")
    private String SMS_ACCESSKEY;

    @Value("${sms.accessKeySecret}")
    private String SMS_ACCESSKEYSECRET;

    @Value("${sms.aliyuncsDomain}")
    private String SMS_ALIYUNCSDOMAIN;

    @Value("${sms.templateCode}")
    private String SMS_TEMPLATECODE;

    @Autowired
    private UmsMemberCacheService memberCacheService;

    private static AsyncClient asyncClient;

    private synchronized AsyncClient getAsyncClient() {
        if (asyncClient == null) {
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                    .accessKeyId(SMS_ACCESSKEY)
                    .accessKeySecret(SMS_ACCESSKEYSECRET)
                    .build());
            asyncClient = AsyncClient.builder()
                    .region("cn-hangzhou") // Region ID
                    //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                    .credentialsProvider(provider)
                    //.serviceConfiguration(Configuration.create()) // Service-level configuration
                    // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    .setEndpointOverride(SMS_ALIYUNCSDOMAIN)
                                    .setConnectTimeout(Duration.ofSeconds(10))
                    )
                    .build();
        }
        return asyncClient;
    }

    @Override
    public boolean sendMessage(String telephone) {
        // Parameter settings for API request
        String authCode = generateAuthCode(telephone);
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder().phoneNumbers(telephone).templateCode(SMS_TEMPLATECODE).templateParam(authCode).build();
//        CompletableFuture<SendSmsResponse> response = getAsyncClient().sendSms(sendSmsRequest);
        getAsyncClient().sendSms(sendSmsRequest);
        return true;
    }

    private String generateAuthCode(String telephone) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        memberCacheService.setAuthCode(telephone, sb.toString());
        return sb.toString();
    }
}
