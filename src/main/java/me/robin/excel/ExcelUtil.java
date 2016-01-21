package me.robin.excel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

/**
 * Created by Lubin.Xuan on 2014/11/14.
 */
public class ExcelUtil {

    private static final ThreadLocal<CellStyle> headerCellStyleThread = new ThreadLocal<>();
    private static final ThreadLocal<CellStyle> dataCellStyleThread = new ThreadLocal<>();
    public static final ThreadLocal<Integer> maxColumnWidth = new ThreadLocal<>();

    public static void initThreadStyle(Workbook workbook) {
        CellStyle headerCellStyle = workbook.createCellStyle();
        CellStyle dataCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.BLACK.index);
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerCellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        CellStyle[] cellStyles = new CellStyle[]{headerCellStyle, dataCellStyle};
        for (CellStyle cellStyle : cellStyles) {
            cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
            cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
            cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
            cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
            cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
            cellStyle.setFont(font);
        }
        headerCellStyleThread.set(headerCellStyle);
        dataCellStyleThread.set(dataCellStyle);
    }

    public static void setMaxColumnWidth(int width){
        maxColumnWidth.set(width);
    }

    public static void createTitleRow(Workbook workbook, Sheet sheet, String title, int columnSize, int rowInSheet) {
        Row row = sheet.createRow(rowInSheet);
        Cell cell = row.createCell(0);
        cell.setCellValue(null == title ? "" : title);
        CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, columnSize - 1);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderBottom(XSSFCellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
        RegionUtil.setBorderTop(XSSFCellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
        RegionUtil.setBorderRight(XSSFCellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
        RegionUtil.setBorderLeft(XSSFCellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        fillCell(workbook, cellStyle, cell);
    }

    public static void createDataRow(Sheet sheet, String[] data, int rowInSheet) {
        Row row = sheet.createRow(rowInSheet);
        for (int t = 0; t < data.length; t++) {
            Cell cell = row.createCell(t);
            cell.setCellValue(null == data[t] ? "" : data[t]);
            if (null != dataCellStyleThread.get()) {
                cell.setCellStyle(dataCellStyleThread.get());
            }
        }
    }

    public static void createHeaderRow(Sheet sheet, String[] data, int rowInSheet) {
        Row row = sheet.createRow(rowInSheet);
        for (int t = 0; t < data.length; t++) {
            Cell cell = row.createCell(t);
            cell.setCellValue(null == data[t] ? "" : data[t]);
            if (null != headerCellStyleThread.get()) {
                cell.setCellStyle(headerCellStyleThread.get());
            }
        }
    }

    public static void fillCell(Workbook workbook, CellStyle cellStyle, Cell cell) {
        cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
        cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
        cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
        cellStyle.setRightBorderColor(HSSFColor.BLACK.index);

        Font font = workbook.createFont();
        font.setColor(HSSFColor.BLACK.index);
        cellStyle.setFont(font);
        cell.setCellStyle(cellStyle);
    }
}