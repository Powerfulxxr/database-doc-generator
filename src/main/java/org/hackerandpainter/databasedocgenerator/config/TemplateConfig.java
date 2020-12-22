package org.hackerandpainter.databasedocgenerator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * 如果自定义配置文件需要外部化配置，参考此类。
 * 如果不外部化 @PropertySource() 和 @ConfigurationProperties() 配合使用classpath下的自定义文件没有问题。
 * @author xxr12
 */
@Slf4j
@Configuration("systemProperties")
public class TemplateConfig {

    @Value("${shelf.template}")
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
