package org.example.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Excel导入+模板下载主界面
 */
public class ReadExcelData extends JFrame {
    private ExcelReaderUtil excelReader = new ExcelReaderUtil();

    public ReadExcelData() {
        // 初始化界面
        initUI();
    }

    /**
     * 初始化界面（导入按钮+下载模板按钮）
     */
    private void initUI() {
        this.setTitle("日常开销Excel导入工具");
        this.setSize(600, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setLocationRelativeTo(null); // 窗口居中

        // 1. 导入Excel按钮
        JButton importBtn = new JButton("选择Excel导入开销数据");
        importBtn.setBounds(50, 50, 200, 40);
        importBtn.addActionListener(new ImportBtnListener());
        this.add(importBtn);

        // 2. 下载模板按钮
        JButton downloadBtn = new JButton("下载Excel导入模板");
        downloadBtn.setBounds(280, 50, 200, 40);
        downloadBtn.addActionListener(new DownloadTemplateListener());
        this.add(downloadBtn);
    }

    /**
     * 导入Excel按钮事件监听
     */
    private class ImportBtnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 打开文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择开销Excel文件（仅支持.xls/.xlsx）");
            // 前端过滤：只显示xls/xlsx
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel文件 (*.xls, *.xlsx)", "xls", "xlsx");
            fileChooser.setFileFilter(filter);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // 显示文件选择框
            int result = fileChooser.showOpenDialog(ReadExcelData.this);
            if (result != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(ReadExcelData.this, "已取消文件选择");
                return;
            }

            // 获取选中文件路径
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();

            // 1. 解析Excel
            List<Expense> expenseList = excelReader.readExcel(filePath);
            Map<Integer, String> errorMap = excelReader.getErrorMsgMap();

            // 2. 处理解析错误
            if (!errorMap.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("Excel解析失败：\n");
                for (Map.Entry<Integer, String> entry : errorMap.entrySet()) {
                    if (entry.getKey() == -1) {
                        errorMsg.append(entry.getValue()).append("\n");
                    } else {
                        errorMsg.append("第").append(entry.getKey()).append("行：").append(entry.getValue()).append("\n");
                    }
                }
                JOptionPane.showMessageDialog(ReadExcelData.this, errorMsg.toString(), "解析失败", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. 批量插入数据库
            try {
                int successCount = DBUtil.batchInsertExpenses(expenseList);
                JOptionPane.showMessageDialog(ReadExcelData.this,
                        "导入成功！共插入 " + successCount + " 条开销数据",
                        "导入成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ReadExcelData.this,
                        "数据库插入失败：" + ex.getMessage(),
                        "插入失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 下载模板按钮事件监听
     */
    private class DownloadTemplateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            downloadTemplate(null, ReadExcelData.this);
        }
    }

    /**
     * 静态方法：根据模板文件名下载相应模板
     * @param templateFileName 模板文件名（如：WaterElectricity.xls），如果为null则让用户选择
     * @param parent 父窗口
     */
    public static void downloadTemplate(String templateFileName, JFrame parent) {
        String fileName = templateFileName;

        // 如果没有指定模板文件名，让用户选择
        if (fileName == null) {
            fileName = chooseTemplateFile(parent);
            if (fileName == null) {
                JOptionPane.showMessageDialog(parent, "已取消模板下载");
                return;
            }
        }

        // 打开保存对话框
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("保存Excel导入模板");
        saveChooser.setSelectedFile(new File(fileName)); // 默认文件名

        int result = saveChooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(parent, "已取消模板下载");
            return;
        }

        // 获取保存路径
        File saveFile = saveChooser.getSelectedFile();
        String savePath = saveFile.getAbsolutePath();

        // 确保后缀正确
        if (!savePath.endsWith(".xls") && !savePath.endsWith(".xlsx")) {
            savePath += fileName.endsWith(".xls") ? ".xls" : ".xlsx";
            saveFile = new File(savePath);
        }

        // 读取项目内模板文件
        try (InputStream is = ReadExcelData.class.getClassLoader().getResourceAsStream("Excel_Template/" + fileName);
             OutputStream os = new FileOutputStream(saveFile)) {

            if (is == null) {
                // 如果指定模板不存在，列出所有可用模板
                String availableTemplates = getAvailableTemplates();
                JOptionPane.showMessageDialog(parent,
                        "模板文件 " + fileName + " 不存在！\n可用模板：\n" + availableTemplates,
                        "下载失败", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 复制模板文件
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            JOptionPane.showMessageDialog(parent,
                    "模板下载成功！保存路径：" + savePath + "\n请按照模板格式填写数据",
                    "下载成功", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "模板下载失败：" + ex.getMessage(),
                    "下载失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 让用户选择要下载的模板文件
     */
    private static String chooseTemplateFile(JFrame parent) {
        Object[] templates = {
                "Communication.xls",
                "Daily Necessities.xls",
                "Entertainment.xls",
                "FoodBeverage.xls",
                "Other.xls",
                "Travel.xls",
                "WaterElectricity.xls"
        };

        String selected = (String) JOptionPane.showInputDialog(
                parent,
                "请选择要下载的模板：",
                "选择模板",
                JOptionPane.QUESTION_MESSAGE,
                null,
                templates,
                templates[0]
        );

        return selected;
    }

    /**
     * 获取可用模板列表
     */
    private static String getAvailableTemplates() {
        return "Communication.xls\n" +
                "Daily Necessities.xls\n" +
                "Entertainment.xls\n" +
                "FoodBeverage.xls\n" +
                "Other.xls\n" +
                "Travel.xls\n" +
                "WaterElectricity.xls";
    }

    /**
     * 主方法：启动程序
     */
    public static void main(String[] args) {
        // Swing界面需在事件调度线程中运行
        SwingUtilities.invokeLater(() -> {
            new ReadExcelData().setVisible(true);
        });
    }
}
