package me.robin.excel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;

public class ExcelPlus {

    private static Logger logger = LoggerFactory.getLogger(ExcelPlus.class);

    private List<Sheet> sheetList;
    private boolean enableIndex;
    private String indexName;

    public ExcelPlus(List<Sheet> sheetList, boolean enableIndex, String indexName) {
        if (null == sheetList) {
            sheetList = new ArrayList<>();
        }
        for (Iterator<Sheet> iterator = sheetList.iterator(); iterator.hasNext(); ) {
            Sheet sheet = iterator.next();
            if (null == sheet) {
                iterator.remove();
            } else if (!sheet.dataExist()) {
                iterator.remove();
            }
        }
        this.sheetList = sheetList;
        this.enableIndex = enableIndex;
        this.indexName = null != indexName && indexName.trim().length() > 0 ? indexName : "序号";
    }

    public static class Sheet {
        private String title;
        private String[] headers;
        private String[][] dataArr;
        boolean separateSheet = false;
        private int sheetSize = 1;
        private String sheetName;
        private int maxWidth = 125 * 256;

        private int nextSheetStart = 1;

        public String getTitle() {
            return title;
        }

        public Sheet setTitle(String title) {
            this.title = title;
            return this;
        }

        public String[] getHeaders() {
            return headers;
        }

        public Sheet setHeaders(String[] headers) {
            this.headers = headers;
            return this;
        }

        public String[][] getDataArr() {
            return dataArr;
        }

        public Sheet setDataArr(String[][] dataArr) {
            this.dataArr = dataArr;
            return this;
        }

        public boolean isSeparateSheet() {
            return separateSheet;
        }

        public Sheet setSeparateSheet(boolean separateSheet) {
            this.separateSheet = separateSheet;
            return this;
        }

        public int getSheetSize() {
            return sheetSize;
        }

        public Sheet setSheetSize(int sheetSize) {
            this.sheetSize = sheetSize;
            return this;
        }

        public String getSheetName() {
            return sheetName;
        }

        public Sheet setSheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public void setNextSheetStart(int nextSheetStart) {
            this.nextSheetStart = nextSheetStart;
        }

        public boolean dataExist() {
            return null != dataArr && dataArr.length > 0;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public Sheet setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth < 4000 ? 4000 : maxWidth;
            return this;
        }
    }

    public void writeToStream(OutputStream os) throws Exception {
        if (sheetList.size() > 0) {
            Workbook workbook = new SXSSFWorkbook();
            ExcelUtil.initThreadStyle(workbook);
            long start = System.currentTimeMillis();
            for (int i = 0; i < sheetList.size(); i++) {
                Sheet sheet = sheetList.get(i);
                ExcelUtil.setMaxColumnWidth(sheet.getMaxWidth());
                logger.debug("write sheet {} {} 数据量 {}", i, sheet.getSheetName(), sheet.dataArr.length);
                if (i == 0) {
                    writeSheet(workbook, sheet, 0);
                } else {
                    writeSheet(workbook, sheet, sheetList.get(i - 1).nextSheetStart);
                }
            }
            logger.debug("Excel 写入完成 耗时 {} ms 输出到 OutputStream [{}]", System.currentTimeMillis() - start, os);
            start = System.currentTimeMillis();
            workbook.write(os);
            os.flush();
            os.close();
            logger.debug("Excel 输出完成 耗时 {} ms", System.currentTimeMillis() - start);
        } else {
            logger.error("Execute Null");
        }
    }

    public void writeSheet(Workbook workbook, Sheet excel, int sheetStart) {
        int rowInSheet = 0;
        int headerRow = null != excel.getHeaders() ? 1 : 0;
        int titleRow = null != excel.getTitle() ? 1 : 0;
        org.apache.poi.ss.usermodel.Sheet sheet = null;
        int sheetCount = 0;
        String[] headers;
        int columnWidth;
        if (enableIndex) {
            headers = ArrayUtils.add(excel.getHeaders(), 0, indexName);
            columnWidth = excel.getDataArr()[0].length + 1;
        } else {
            headers = excel.getHeaders();
            columnWidth = excel.getDataArr()[0].length;
        }
        if (null == headers) {
            headers = new String[0];
        }
        Map<Integer, Integer[]> columnWidthMap = new HashMap<>();
        long start = System.currentTimeMillis();
        long l_start = System.currentTimeMillis();
        for (int i = 0; i < excel.getDataArr().length; i++) {
            if (rowInSheet == 0) {
                if (null != excel.getSheetName() && excel.getSheetName().trim().length() > 0) {
                    if (excel.isSeparateSheet()) {
                        sheet = workbook.createSheet(excel.getSheetName().trim() + "-" + (sheetCount + 1));
                    } else {
                        sheet = workbook.createSheet(excel.getSheetName().trim());
                    }
                } else {
                    sheet = workbook.createSheet();
                }
                columnWidthMap.put(sheetCount, new Integer[columnWidth]);
                if (titleRow == 1) {
                    ExcelUtil.createTitleRow(workbook, sheet, excel.getTitle(), headers.length, 0);
                }
                if (headerRow == 1) {
                    ExcelUtil.createHeaderRow(sheet, headers, titleRow);
                    colWidth(columnWidthMap.get(sheetCount), headers);
                }
                sheetCount++;
            }
            String[] dataRow;
            if (enableIndex) {
                dataRow = ArrayUtils.add(excel.getDataArr()[i], 0, String.valueOf((sheetCount - 1) * excel.getSheetSize() + rowInSheet + 1));
            } else {
                dataRow = excel.getDataArr()[i];
            }
            ExcelUtil.createDataRow(sheet, dataRow, rowInSheet + headerRow + titleRow);
            if (i != 0 && (i + 1) % 100 == 0) {
                logger.debug("数据写入 [{}]行 耗时 {} ms  Sheet [{}]", i, System.currentTimeMillis() - start, sheet.getSheetName());
                start = System.currentTimeMillis();
            }
            colWidth(columnWidthMap.get(sheetCount - 1), dataRow);
            rowInSheet++;
            if (excel.isSeparateSheet() && rowInSheet >= excel.getSheetSize()) {
                rowInSheet = 0;
            }
        }
        excel.setNextSheetStart(sheetStart + sheetCount);
        logger.debug("Excel 数据集写完 耗时 {} ms 开始重设excel 宽度", System.currentTimeMillis() - l_start);
        start = System.currentTimeMillis();
        for (int i = 0; i < sheetCount; i++) {
            sheet = workbook.getSheetAt(sheetStart + i);
            Integer[] columnWidthArr = columnWidthMap.get(i);
            for (int j = 0; j < columnWidth; j++) {
                if (null != columnWidthArr[j] && columnWidthArr[j] > 0) {
                    sheet.setColumnWidth(j, columnWidthArr[j]);
                }
            }
        }
        logger.debug("重设excel 宽度完成 耗时 {} ms", System.currentTimeMillis() - start);
    }

    private void colWidth(Integer columnWidth[], String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (null != values[i] && values[i].trim().length() > 1) {
                int width = (int) (values[i].getBytes().length * 256 * 1.1);
                int width2 = values[i].getBytes().length * 256 + 4 * 256;
                width = width2 < width ? width2 : width;
                width = ExcelUtil.maxColumnWidth.get() < width ? ExcelUtil.maxColumnWidth.get() : width;
                if (null == columnWidth[i] || columnWidth[i] < width) {
                    columnWidth[i] = width;
                }
            }
        }
    }
}