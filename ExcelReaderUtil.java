package org.example.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Excel解析工具类（支持xls/xlsx，数据验证）
 */
public class ExcelReaderUtil {
    // 合法开销类型
    private static final Set<String> VALID_EXPENSE_TYPES = new HashSet<>(Arrays.asList(
            "出行", "通讯", "水电", "食品餐饮", "娱乐", "生活日用品", "其他"
    ));
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Map<Integer, String> errorMsgMap = new HashMap<>(); // 解析错误信息

    /**
     * 读取Excel并解析为Expense列表
     */
    public List<Expense> readExcel(String filePath) {
        List<Expense> expenseList = new ArrayList<>();
        File file = new File(filePath);
        errorMsgMap.clear();

        // 基础校验：文件是否存在
        if (!file.exists()) {
            errorMsgMap.put(-1, "文件不存在：" + filePath);
            return expenseList;
        }

        Workbook workbook = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            // 区分xls/xlsx
            if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else {
                errorMsgMap.put(-1, "仅支持.xls/.xlsx格式！");
                return expenseList;
            }

            // 读取第一个Sheet，跳过表头（第1行）
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                errorMsgMap.put(-1, "Excel无有效工作表！");
                return expenseList;
            }

            int startRow = 1; // 表头行（第1行）跳过，从第2行开始读
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < startRow) {
                errorMsgMap.put(-1, "Excel无有效数据行！");
                return expenseList;
            }

            // 逐行解析
            for (int rowNum = startRow; rowNum <= lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    // 跳过完全空的行，不记录错误
                    continue;
                }
                Expense expense = parseRow(row, rowNum + 1); // 行号+1（用户视角）
                if (expense != null) {
                    expenseList.add(expense);
                }
            }

        } catch (Exception e) {
            errorMsgMap.put(-1, "Excel读取失败：" + e.getMessage());
            e.printStackTrace(); // 添加调试信息
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    errorMsgMap.put(-1, "关闭Excel失败：" + e.getMessage());
                }
            }
        }
        return expenseList;
    }

    /**
     * 解析单行数据并验证格式
     */
    private Expense parseRow(Row row, int showRowNum) {
        Expense expense = new Expense();
        try {
            // 1. 金额（第1列，必填，数字>0）- 索引0
            Cell amountCell = row.getCell(0);
            if (amountCell == null || amountCell.getCellType() == CellType.BLANK) {
                errorMsgMap.put(showRowNum, "金额为空（必填项）");
                return null;
            }
            Double amount = getCellValueAsDouble(amountCell);
            if (amount == null || amount <= 0) {
                errorMsgMap.put(showRowNum, "金额格式错误（需为大于0的数字）：" + getCellValueAsString(amountCell));
                return null;
            }
            expense.setAmount(amount);

            // 2. 开销类型（第2列，必填，合法值）- 索引1
            Cell typeCell = row.getCell(1);
            if (typeCell == null || typeCell.getCellType() == CellType.BLANK) {
                errorMsgMap.put(showRowNum, "开销类型为空（必填项）");
                return null;
            }
            String type = getCellValueAsString(typeCell).trim();
            if (!VALID_EXPENSE_TYPES.contains(type)) {
                errorMsgMap.put(showRowNum, "开销类型不合法（仅支持：出行/通讯/水电/食品餐饮/娱乐/生活日用品/其他）：" + type);
                return null;
            }
            expense.setExpenseType(type);

            // 3. 支付方式（第3列，必填）- 索引2
            Cell payCell = row.getCell(2);
            if (payCell == null || payCell.getCellType() == CellType.BLANK) {
                errorMsgMap.put(showRowNum, "支付方式为空（必填项）");
                return null;
            }
            expense.setPaymentMethod(getCellValueAsString(payCell).trim());

            // 4. 消费时间（第4列，必填，yyyy-MM-dd）- 索引3
            Cell timeCell = row.getCell(3);
            if (timeCell == null || timeCell.getCellType() == CellType.BLANK) {
                errorMsgMap.put(showRowNum, "消费时间为空（必填项）");
                return null;
            }
            String timeStr = getCellValueAsString(timeCell).trim();
            try {
                expense.setTime(DATE_FORMAT.parse(timeStr));
            } catch (ParseException e) {
                errorMsgMap.put(showRowNum, "时间格式错误（需为yyyy-MM-dd）：" + timeStr);
                return null;
            }

            // 5. 备注（第5列，选填）- 索引4
            Cell remarkCell = row.getCell(4);
            expense.setRemarks(remarkCell == null ? "" : getCellValueAsString(remarkCell).trim());

            // 6. 用户名（第6列，必填）- 索引5
            Cell userCell = row.getCell(5);
            if (userCell == null || userCell.getCellType() == CellType.BLANK) {
                errorMsgMap.put(showRowNum, "用户名为空（必填项）");
                return null;
            }
            String userName = getCellValueAsString(userCell).trim();
            if (userName.isEmpty()) {
                errorMsgMap.put(showRowNum, "用户名为空（必填项）");
                return null;
            }
            expense.setUserName(userName);

            // 调试信息
            System.out.println("解析第" + showRowNum + "行数据: " +
                    "金额=" + expense.getAmount() +
                    ", 类型=" + expense.getExpenseType() +
                    ", 支付方式=" + expense.getPaymentMethod() +
                    ", 时间=" + expense.getTime() +
                    ", 备注=" + expense.getRemarks() +
                    ", 用户名=" + expense.getUserName());

            return expense;

        } catch (Exception e) {
            errorMsgMap.put(showRowNum, "数据解析异常：" + e.getMessage());
            e.printStackTrace(); // 添加调试信息
            return null;
        }
    }

    /**
     * 单元格值转字符串（兼容所有类型）
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                } else {
                    // 避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 单元格值转Double
     */
    private Double getCellValueAsDouble(Cell cell) {
        String val = getCellValueAsString(cell).trim();
        if (val.isEmpty()) return null;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取解析错误信息
     */
    public Map<Integer, String> getErrorMsgMap() {
        return errorMsgMap;
    }
}
