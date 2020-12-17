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
import java.util.function.Consumer;

/**
 * DatabaseDocService
 *
 * @author zt
 * @version 2018/10/6 0006
 */
public class MySQL extends Generator {
    String sqlTables = "select table_name,table_comment from information_schema.tables where table_schema = '@dbname'" +
            " order by table_name asc";
    String sqlColumns = "select column_name,column_type,column_key,is_nullable,column_comment from information_schema" +
            ".columns where table_schema = '@dbname'  and table_name " +
            "='@tablename'";

    public MySQL(String dbName, SimpleDataSource dataSource, HttpServletRequest request) {
        super(dbName, dataSource,request);
    }

    @Override
    public List<TableVo> getTableData() {
        String tableName = this.request.getParameter("tableName");
        String sql = sqlTables.replace("@dbname", dbName);
        List<Record> list = getList(sql);
        List<TableVo> tables = new ArrayList<>();
        if (StringUtils.isNotBlank(tableName)) {
            List<String> split = Arrays.asList(tableName.split(","));
            list.parallelStream()
                    .filter(x->split.stream().anyMatch(xx->xx.equals(x.getString("table_name"))))
                    .forEach(getRecordConsumer(tables,(table, comment) -> getTableInfo(table,comment)));
        }else {
            list.parallelStream().forEach(getRecordConsumer(tables,(table, comment) -> getTableInfo(table,comment)));
        }
        return tables;
    }

    public TableVo getTableInfo(String table, String comment) {
        TableVo tableVo = new TableVo();
        tableVo.setTable(table);
        tableVo.setComment(comment);
        String sql = sqlColumns.replace("@dbname", dbName);
        sql = sql.replace("@tablename", table);
        List<Record> list = getList(sql);
        List<ColumnVo> columns = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Record record = list.get(i);
            ColumnVo column = new ColumnVo();
            column.setName(record.getString("column_name"));
            column.setType(record.getString("column_type"));
            column.setKey(record.getString("column_key"));
            column.setIsNullable(record.getString("is_nullable").equals("NO") ? "否" : "是");
            column.setComment(record.getString("column_comment"));
            columns.add(column);
        }
        tableVo.setColumns(columns);
        return tableVo;
    }

}