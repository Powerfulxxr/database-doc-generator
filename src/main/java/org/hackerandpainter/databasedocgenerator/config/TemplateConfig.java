package org.hackerandpainter.databasedocgenerator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


/**
 * 如果自定义配置文件需要外部化配置，参考此类。
 * 定义配置文件文件命名按application-xxxx.properties
 * spring.profiles.include=xxxx
 * @author xxr12
 */
@Component
@PropertySource("classpath:application-common.properties")
@ConfigurationProperties(prefix = "shelf")
public class TemplateConfig {

    private String template;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "TemplateConfig{" +
                "template='" + template + '\'' +
                '}';
    }
}
