package org.hackerandpainter.databasedocgenerator.database;

import org.apache.commons.lang3.StringUtils;
import org.hackerandpainter.databasedocgenerator.bean.ColumnVo;
import org.hackerandpainter.databasedocgenerator.bean.TableVo;
import org.nutz.dao.entity.Record;
import org.nutz.dao.impl.SimpleDataSource;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PostgreSQL
 *
 * @author zt
 * @version 2019/1/8 0008
 */
public class PostgreSQL extends Generator {
    private String sqlTables = "SELECT A .oid, A .relname AS NAME, b.description AS COMMENT " +
            "FROM pg_class A LEFT OUTER JOIN pg_description b ON b.objsubid = 0 AND A .oid = b.objoid " +
            "WHERE A .relnamespace = ( SELECT oid FROM pg_namespace WHERE nspname = 'public' ) AND A .relkind = 'r' ORDER BY A .relname";
    private String sqlColumns = "SELECT a.attname AS field,t.typname AS type,a.attlen AS length,a.atttypmod AS lengthvar,a.attnotnull AS notnull,b.description AS comment" +
            " FROM pg_class c,pg_attribute a LEFT OUTER JOIN pg_description b ON a.attrelid=b.objoid AND a.attnum = b.objsubid,pg_type t" +
            " WHERE c.relname = '@tablename' and a.attnum > 0 and a.attrelid = c.oid and a.atttypid = t.oid" +
            " ORDER BY a.attnum";

    public PostgreSQL(String dbName, SimpleDataSource dataSource, HttpServletRequest request) {
        super(dbName, dataSource,request);
    }

    @Override
    public List<TableVo> getTableData() {
        String tableName =AssertNUll(request.getAttribute("tableName"));
        List<Record> list = getList(sqlTables);
        List<TableVo> tables = new ArrayList<>();
        if (StringUtils.isNotBlank(tableName)) {
            List<String> split = Arrays.asList(tableName.split(","));
            list.stream().filter(x->split.stream().anyMatch(xx->xx.equals(x.getString("name"))))
                    .forEach(getRecordConsumer("name","comment",tables,(table, comment) -> getTableInfo(table,comment)));
        }else {
            list.parallelStream().forEach(getRecordConsumer("name","comment",tables,(table, comment) -> getTableInfo(table,comment)));
        }
        return tables;
    }
    public TableVo getTableInfo(String table, String tableComment){
        TableVo tableVo = new TableVo();
        tableVo.setTable(table);
        tableVo.setComment(tableComment);
        String sql = sqlColumns.replace("@tablename",table);

        List<Record> columns = getList(sql);

        List<ColumnVo> columnVoList =  new ArrayList<>();
        for(int i=0;i<columns.size();i++){
            Record record = columns.get(i);
            ColumnVo column = new ColumnVo();
            column.setName(record.getString("field"));
            column.setType(record.getString("type"));
            column.setIsNullable(record.getString("notnull").equals("true")?"否":"是");
            column.setComment(record.getString("comment"));
            columnVoList.add(column);
        }
        tableVo.setColumns(columnVoList);
        return tableVo;
    }
}
