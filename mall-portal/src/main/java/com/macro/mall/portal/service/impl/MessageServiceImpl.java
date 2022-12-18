package com.macro.mall.portal.service.impl;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.macro.mall.common.exception.CaiShiFuException;
import com.macro.mall.portal.service.MessageService;
import com.macro.mall.portal.service.UmsMemberCacheService;
import darabonba.core.client.ClientOverrideConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Value("${sms.accessKey}")
    private String SMS_ACCESSKEY;

    @Value("${sms.accessKeySecret}")
    private String SMS_ACCESSKEYSECRET;

    @Value("${sms.aliyuncsDomain}")
    private String SMS_ALIYUNCSDOMAIN;

    @Value("${sms.templateCode}")
    private String SMS_TEMPLATECODE;

    @Value("${sms.signname}")
    private String SMS_SIGNNAME;

    @Autowired
    private UmsMemberCacheService memberCacheService;

    private static AsyncClient asyncClient;

    private synchronized AsyncClient getAsyncClient() {
        if (asyncClient == null) {
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                    .accessKeyId(getSMS_ACCESSKEY())
                    .accessKeySecret(getSMS_ACCESSKEYSECRET())
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
    public boolean sendMessage(String telephone) throws CaiShiFuException {
        // Parameter settings for API request
        String authCode = generateAuthCode(telephone);
        String  temp = String.format("{\"code\":\""+ authCode +"\"}");
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder().signName(SMS_SIGNNAME).phoneNumbers(telephone).templateCode(SMS_TEMPLATECODE).templateParam(temp).build();
//        CompletableFuture<SendSmsResponse> response = getAsyncClient().sendSms(sendSmsRequest);
        CompletableFuture<SendSmsResponse> response  = getAsyncClient().sendSms(sendSmsRequest);
        try {
            response.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("sendMessage to %s InterruptedException",telephone,e);
            throw new CaiShiFuException(e);
        }
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

    public String getSMS_ACCESSKEY() {
        return new String(Base64.getDecoder().decode(SMS_ACCESSKEY.getBytes(StandardCharsets.UTF_8)));
    }

    public String getSMS_ACCESSKEYSECRET() {
        return new String(Base64.getDecoder().decode(SMS_ACCESSKEYSECRET.getBytes(StandardCharsets.UTF_8)));
    }
}
