package com.lizhi.common.excel.export.xls;

import com.lizhi.common.excel.common.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhi.li
 * @Description
 * @created 2018/12/16 18:15
 */
public class ExportTest {

    public static void main(String[] args) throws Exception {

        //用于存放第一行单元格
        List<Column> listColumn = new ArrayList<>();
        // 用于存放第一列第二行的单元格
        List<Column> list2 = new ArrayList<>();
        // 创建一列，value1 表示这一列需要导出字段的值
        list2.add(new Column("标题1","value1"));
        list2.add(new Column("标题2","value1"));
        list2.add(new Column("标题3","value1"));

        // 用于存放第二列第二行的单元格
        List<Column> list3  = new ArrayList<>();
        list3.add(new Column("标题6","value2"));
        list3.add(new Column("标题7","value2"));


        //创建第一行大标题,大标题的fieldName 为 null
        Column c1 = new Column("标题1",null);
        c1.setListColumn(list2);
        Column c2 = new Column("标题2",null);
        c2.setListColumn(list3);
        listColumn.add(c1);
        listColumn.add(c2);

        //需要导出的数据
        List<ValueObj> valueList = new ArrayList<>();
        valueList.add(new ValueObj("1","11"));
        valueList.add(new ValueObj("2","22"));
        valueList.add(new ValueObj("3","33"));
        valueList.add(new ValueObj("4","44"));

        ExcelHelper<ValueObj> ta = new ExcelHelper<ValueObj>("表格",15,20);
        ta.exportExcel(listColumn, valueList,"D:\\outExcel.xls");
    }
}
