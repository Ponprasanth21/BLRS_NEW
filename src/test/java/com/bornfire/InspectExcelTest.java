package com.bornfire;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileInputStream;

public class InspectExcelTest {

    @Test
    public void testReadExcel() {
        try {
            File file = new File("src/main/resources/static/DOCUMENT/Upload_Format.xls");
            System.out.println("Excel file absolute path: " + file.getAbsolutePath());
            if (!file.exists()) {
                System.out.println("ERROR: File does not exist!");
                return;
            }
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new HSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                System.out.println("Sheet Name: " + sheet.getSheetName());
                int rows = sheet.getPhysicalNumberOfRows();
                System.out.println("Number of rows: " + rows);
                
                for (int r = 0; r < Math.min(rows, 10); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        System.out.println("Row " + r + " is null");
                        continue;
                    }
                    System.out.print("Row " + r + ": ");
                    int cells = row.getLastCellNum();
                    for (int c = 0; c < cells; c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) {
                            System.out.print("[null] | ");
                            continue;
                        }
                        switch (cell.getCellTypeEnum()) {
                            case STRING:
                                System.out.print(cell.getStringCellValue() + " | ");
                                break;
                            case NUMERIC:
                                System.out.print(cell.getNumericCellValue() + " | ");
                                break;
                            case BOOLEAN:
                                System.out.print(cell.getBooleanCellValue() + " | ");
                                break;
                            default:
                                System.out.print("[" + cell.getCellTypeEnum() + "] | ");
                        }
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
