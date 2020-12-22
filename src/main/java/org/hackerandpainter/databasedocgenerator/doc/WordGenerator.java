package org.hackerandpainter.databasedocgenerator.doc;

import cn.hutool.poi.word.WordUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.hackerandpainter.databasedocgenerator.bean.TableVo;
import org.hackerandpainter.databasedocgenerator.config.TemplateConfig;
import org.hackerandpainter.databasedocgenerator.unitl.SpringContextUtil;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WordGenerator
 *
 * @author zt
 * @version 2019/1/12 0012
 */
public class WordGenerator {

    private WordGenerator() {
        throw new AssertionError();
    }

    public static void createDoc(String dbName, List<TableVo> list, String savePath) {
        Map map = new HashMap();
        map.put("dbName", dbName);
        map.put("tables", list);
        try {
            Configuration configuration = SpringContextUtil.getBean(FreeMarkerConfigurer.class).getConfiguration();
            TemplateConfig templateConfig = SpringContextUtil.getBean(TemplateConfig.class);
            String templatePath = templateConfig.getTemplate();
            configuration.setDefaultEncoding("utf-8");
            if(StringUtils.isBlank(templatePath)) {
                //加载classpath下的模板
                configuration.setClassForTemplateLoading(WordUtil.class, "/templates");
            }else {
                //加载路径下的模板
                configuration.setDirectoryForTemplateLoading(new File(templatePath));
            }
            Template template = configuration.getTemplate("/database.html");
            String name =  File.separator + dbName + ".html";
            File f = new File(savePath + name);
            Writer w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
            template.process(map, w);
            w.close();
            new Html2DocConverter(savePath + File.separator + dbName + ".html", savePath + File
                    .separator + dbName + ".doc")
                    .writeWordFile();
        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

}
