package com.lizhi.common.excel.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhi.li
 * @Description 单元格类
 * @created 2018/12/16 17:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
    /**
     * 单元格内容
     */
    private String content;

    /**
     * 字段名称，用户导出表格时反射调用
     */
    private String fieldName;

    /**
     * 单元格的集合
     */
    private List<Column> listColumn = new ArrayList<>();

    public Column(String content,String fieldName){
        this.content = content;
        this.fieldName = fieldName;
    }

}
