package org.hackerandpainter.databasedocgenerator.doc;

import cn.hutool.poi.word.WordUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.hackerandpainter.databasedocgenerator.bean.TableVo;
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
            configuration.setDefaultEncoding("utf-8");
            configuration.setClassForTemplateLoading(WordUtil.class,"/");
            Template template = configuration.getTemplate("templates/database.html");
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
