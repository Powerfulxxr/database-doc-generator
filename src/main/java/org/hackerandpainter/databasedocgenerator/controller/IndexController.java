package org.hackerandpainter.databasedocgenerator.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ZipUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.hackerandpainter.databasedocgenerator.database.*;
import org.nutz.dao.impl.SimpleDataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * @Description
 * @Author
 * @Date
 **/
@RestController
public class IndexController {

    @GetMapping("/generator")
    @ApiImplicitParams({
            @ApiImplicitParam(name="dbType", value = "0.数据库方言\n1:MySQL\n2:Oracle\n3:PostgreSQL\n4:SQLServer", defaultValue = "1",allowableValues = "1,2,3,4",required = true,collectionFormat = "1"),
            @ApiImplicitParam(name = "ip", value = "1.ip地址", defaultValue = "localhost", required = true),
            @ApiImplicitParam(name = "databaseName", value = "2.数据库名", required = true),
            @ApiImplicitParam(name = "port", value = "3.端口", defaultValue = "3306", required = true),
            @ApiImplicitParam(name = "userName", value = "4.用户名", defaultValue = "root", required = true),
            @ApiImplicitParam(name = "password", value = "5.密码", defaultValue = "root", required = true),
            @ApiImplicitParam(name = "tableName", value = "6数据库表名(多个表需英文逗号分开,空值表示生成所有表)"),
            @ApiImplicitParam(name="isZip", value = "7.是否压缩", defaultValue = "否",allowableValues = "否,是",required = true,collectionFormat = "1"),
            @ApiImplicitParam(name = "serviceName", value = "8.Oracle数据库才需要", required = false),
            @ApiImplicitParam(name = "path", value = "8.文档生成路径", required =true )
    })
    public String generatorDatabaseDoc(HttpServletResponse response, String dbType,
                                       @RequestParam(defaultValue = "localhost") String ip,
                                       String databaseName,
                                       @RequestParam(defaultValue = "3306") String port,
                                       @RequestParam(defaultValue = "postgres") String userName,
                                       @RequestParam(defaultValue = "root") String password,
                                       String tableName,
                                       @RequestParam(defaultValue = "否") String isZip,
                                       @RequestParam(required = false) String serviceName,
                                       String path) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.setAttribute("tableName", tableName);
        request.setAttribute("path", path);
        if ("c".equals(dbType)) {
            System.exit(-1);
        }
        if (Integer.valueOf(dbType) < 1 || Integer.valueOf(dbType) > 4) {
            System.out.println("wrong number,will exit");
            System.exit(-1);
        }
        if ("2".equals(dbType)) {
            System.out.println("input service name:");
        }
        if ("1".equals(dbType) || "3".equals(dbType) || "4".equals(dbType)) {
            System.out.println("input database name:");
        }

        SimpleDataSource dataSource = new SimpleDataSource();
        if ("1".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + databaseName
                    + "?characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true");
        } else if ("2".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:oracle:thin:@" + ip + ":" + port + ":" + serviceName);
        } else if ("3".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:postgresql://" + ip + ":" + port + "/" + databaseName);
        } else if ("4".equals(dbType)) {
            dataSource.setJdbcUrl("jdbc:sqlserver://" + ip + ":" + port + ";database=" + databaseName);
        }

        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        Generator generator = null;

        switch (dbType) {
            case "1":
                generator = new MySQL(databaseName, dataSource,request);
                break;
            case "2":
                generator = new Oracle(userName, dataSource,request);
                break;
            case "3":
                generator = new PostgreSQL(databaseName, dataSource,request);
                break;
            case "4":
                generator = new SqlServer(databaseName, dataSource,request);
        }

        String savePath = generator.generateDoc();
        zipFile(response, isZip, savePath);
//        FileUtil.del(new File(savePath).getParent());
        return "文档生成成功";
    }

    /**
     * 压缩文件
     * @param response
     * @param isZip
     * @param savePath
     * @throws IOException
     */
    private void zipFile(HttpServletResponse response, String isZip, String savePath) throws IOException {
        if("是".equals(isZip)) {
            //压缩文件
            ZipUtil.zip(savePath, savePath + ".zip");
            File file = new File(savePath + ".zip");
            //1、设置response 响应头
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + URLEncoder.encode(file.getName(), "UTF-8"));
            //2、 读取文件--输入流
            InputStream input = new FileInputStream(file);
            //3、 写出文件--输出流
            OutputStream out = response.getOutputStream();
            byte[] buff = new byte[1024];
            int index = 0;
            //4、执行 写出操作
            while ((index = input.read(buff)) != -1) {
                out.write(buff, 0, index);
                out.flush();
            }
            out.close();
            input.close();
        }
    }
}
