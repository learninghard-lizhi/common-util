package com.lizhi.common.excel.export.xls;

import com.lizhi.common.excel.common.Column;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhi.li
 * @Description
 * @created 2018/12/16 17:16
 */
@Getter
@Setter
public class ExcelHelper<T> {
    /**
     * 表格标题
     */
    private String title;

    /**
     * 单元格宽度
     */
    private int colWidth = 20;

    /**
     * 行高度
     */
    private int rowHeight = 20;

    private HSSFWorkbook workbook;

    /**
     * 表头样式
     */
    private HSSFCellStyle headStyle;

    /**
     * 主体样式
     */
    private HSSFCellStyle bodyStyle;

    /**
     * 日期格式化,默认yyyy-MM-dd HH:mm:ss
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor
     * @param title
     */
    public ExcelHelper(String title){
        this.title = title;
        workbook = new HSSFWorkbook();
        init();
    }
    /**
     * Constructor
     * @param title
     * @param colWidth
     * @param rowHeight
     */
    public ExcelHelper(String title, int colWidth, int rowHeight){
        this.colWidth = colWidth;
        this.rowHeight = rowHeight;
        this.title = title;
        workbook = new HSSFWorkbook();
        init();
    }

    /**
     * Constructor
     * @param title
     * @param colWidth
     * @param rowHeight
     * @param dateFormat
     */
    public ExcelHelper(String title, int colWidth, int rowHeight, String dateFormat) {
        this.title = title;
        this.colWidth = colWidth;
        this.rowHeight = rowHeight;
        workbook = new HSSFWorkbook();
        sdf = new SimpleDateFormat(dateFormat);
        init();
    }

    /**
     * 导出Excel,适用于web导出excel
     *
     * @param sheet
     * @param data
     */
    private void writeSheet(HSSFSheet sheet, List<T> data,List<Column> headerList) {
        try {
            sheet.setDefaultColumnWidth(colWidth);
            sheet.setDefaultRowHeightInPoints(rowHeight);
            createHead(headerList, sheet);
            writeSheetContent(headerList, data, sheet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 导出表格
     * @param listColumn
     * @param datas
     * @return
     * @throws Exception
     */
    public InputStream exportExcel(List<Column> listColumn,List<T> datas) throws Exception {
        splitDataToSheets(datas,listColumn);
        return save(workbook);
    }

    /**
     * 导出表格 支持2级表头或单表头的Excel导出
     * @param headers
     * @param datas
     * @param filePath
     * @throws FileNotFoundException
     * @throws IOException
     * void
     */
    public void exportExcel(List<Column> headers,List<T> datas,String filePath) throws IOException {
        splitDataToSheets(datas, headers);
        save(workbook, filePath);
    }

    /**
     * 把数据写入到单元格
     * @param listColumn
     * @param datas
     * @param sheet
     * @throws Exception
     * void
     */
    private void writeSheetContent(List<Column> listColumn,List<T> datas,HSSFSheet sheet) throws Exception {
        HSSFRow row;
        List<Column> listCol = getColumnList(listColumn);
        for (int i = 0, index = 2; i < datas.size(); i++, index++) {
            // 创建行
            row = sheet.createRow(index);
            for (int j = 0; j < listCol.size(); j++) {
                Column c = listCol.get(j);
                createCol(row, c, datas.get(i), j);
            }
        }
    }

    /**
     * 创建表头
     * @param listColumn 表头数组
     * @return 返回表头总行数
     */
    public void createHead(List<Column> listColumn, HSSFSheet sheetCo){
        HSSFRow row = sheetCo.createRow(0);
        HSSFRow row2 = sheetCo.createRow(1);
        for(short i = 0, n = 0; i < listColumn.size(); i++){
            HSSFCell cell1 = row.createCell(n);
            cell1.setCellStyle(headStyle);
            HSSFRichTextString text;
            List<Column> columns = listColumn.get(i).getListColumn();
            //双标题
            if(CollectionUtils.isEmpty(columns)){
                // 单标题
                HSSFCell cell2 = row2.createCell(n);
                cell2.setCellStyle(headStyle);
                text = new HSSFRichTextString(listColumn.get(i).getContent());
                sheetCo.addMergedRegion(new CellRangeAddress(0, n, 1, n));
                n++;
                cell1.setCellValue(text);
                continue;
            }

            text = new HSSFRichTextString(listColumn.get(i).getContent());
            cell1.setCellValue(text);

            // 创建第一行大标题
            sheetCo.addMergedRegion(new CellRangeAddress(0, 0, n, (short) (n + columns.size() -1)));
            // 给标题赋值
            for(int j = 0; j < columns.size(); j++){
                HSSFCell cell2 = row2.createCell(n++);
                cell2.setCellStyle(headStyle);
                cell2.setCellValue(new HSSFRichTextString(columns.get(j).getContent()));
            }
        }
    }

    /**
     * 创建行
     * @param row
     * @param column
     * @param v
     * @param rowIndex
     * @return
     * @throws Exception
     */
    public int createRowVal(HSSFRow row, Column column,T v,int rowIndex) throws Exception{
        // 遍历标题
        if(column.getListColumn() != null && column.getListColumn().size() > 0){
            for(int i = 0; i < column.getListColumn().size(); i++){
                createRowVal(row,column.getListColumn().get(i),v,rowIndex);
            }
        }else{
            createCol(row,column,v,rowIndex++);
        }

        return rowIndex;
    }

    /**
     * 创建单元格
     * @param row
     * @param column
     * @param v
     * @param columnIndex
     * @throws Exception
     */
    public void createCol(HSSFRow row,Column column,T v,int columnIndex) throws Exception{
        // 创建单元格
        HSSFCell cell = row.createCell(columnIndex);
        // 设置单元格样式
        cell.setCellStyle(bodyStyle);
        Class cls = v.getClass();
        Field field = cls.getDeclaredField(column.getFieldName());
        // 设置些属性是可以访问的
        field.setAccessible(true);
        if(field.get(v) != null){
            Object value = field.get(v);
            if(value instanceof Date){
                value = parseDate((Date)value);
            }

            HSSFRichTextString richString = new HSSFRichTextString(value.toString());
            cell.setCellValue(richString);
        }
    }

    /**
     * init
     */
    private void init(){
        // 生成表头样式
        headStyle = workbook.createCellStyle();
        headStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.AQUA.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 生成一个字体
        HSSFFont headFont = workbook.createFont();
        headFont.setColor(HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
        headFont.setFontHeightInPoints((short) 12);
        headFont.setBold(true);
        // 把字体应用到当前的样式
        headStyle.setFont(headFont);

        // 生成并设置另一个样式
        bodyStyle = workbook.createCellStyle();
        bodyStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        bodyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setAlignment(HorizontalAlignment.CENTER);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 生成另一个字体
        HSSFFont bodyFont = workbook.createFont();
        bodyFont.setBold(false);
        // 把字体应用到当前的样式
        bodyStyle.setFont(bodyFont);
    }

    /**
     * 时间转换
     * @param date
     * @return
     * String
     */
    private  String parseDate(Date date){
        String dateStr = "";
        try{
            dateStr = sdf.format(date);
        } catch (Exception e){
            e.printStackTrace();
        }

        return dateStr;
    }

    /**
     * 拆分sheet，因为每个sheet不能超过6526，否则会报异常
     * @param data
     * @param listColumn
     * void
     */
    private void splitDataToSheets(List<T> data,List<Column> listColumn) {
        int dataCount = data.size();
        int maxColumn = 65535;
        int pieces = dataCount / maxColumn;
        for (int i = 1; i <= pieces; i++) {
            HSSFSheet sheet = workbook.createSheet(this.title + i);
            List<T> subList = data.subList((i - 1) * maxColumn, i * maxColumn);
            writeSheet(sheet, subList, listColumn);
        }

        HSSFSheet sheet = workbook.createSheet(this.title + (pieces + 1));
        writeSheet(sheet, data.subList(pieces * maxColumn, dataCount), listColumn);
    }

    /**
     * 把column的columnList整理成一个list<column>
     * @param listColumn
     * @return
     * List<Column>
     */
    private List<Column> getColumnList(List<Column> listColumn){
        List<Column> listCol = new ArrayList<>();
        for(int i = 0; i < listColumn.size(); i++){
            List<Column> list = listColumn.get(i).getListColumn();
            if(list.size() > 0){
                for(Column c : list){
                    if(c.getFieldName() != null){
                        listCol.add(c);
                    }
                }
            }else{
                if(listColumn.get(i).getFieldName() != null){
                    listCol.add(listColumn.get(i));
                }
            }
        }

        return listCol;
    }

    /**
     * 保存Excel到InputStream，此方法适合web导出excel
     *
     * @param workbook
     * @return
     */
    private InputStream save(HSSFWorkbook workbook) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            workbook.write(bos);
            InputStream bis = new ByteArrayInputStream(bos.toByteArray());
            return bis;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存文件
     * @param workbook
     * @param filePath
     * @throws IOException
     */
    private void save(HSSFWorkbook workbook,String filePath) throws IOException {
        workbook.write(new FileOutputStream(filePath));
    }
}
