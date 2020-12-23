package org.hackerandpainter.databasedocgenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

import java.io.IOException;

/**
 * @author xxr12
 */
@Profile("window")
@Slf4j
@Configuration
public class GuideConfig {

    @Value("${server.address}")
    private String address;
    @Value("${server.port}")
    private String port;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @EventListener({ApplicationReadyEvent.class})
    void applicationReadyEvent() {
//        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        StringBuffer sb = new StringBuffer();
        sb.append("http://").append(address).append(":").append(port).append(contextPath).append("/swagger-ui.html");
//        sb.append("http://172.20.37.86:1020/swagger-ui.html");
        log.info("*****************   应用已经准备就绪 ... 启动浏览器  ******************");
        String url = sb.toString();
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
