package org.example.ui;

import org.example.util.DBUtil;
import org.example.util.ExcelReaderUtil;
import org.example.util.Expense;
import org.example.util.ReadExcelData;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class WaterElectricityJFrame extends JFrame implements MouseListener {

    JTextField amountField = new JTextField();
    JTextField paymentMethodField = new JTextField();
    JTextField timeField = new JTextField();
    JTextField remarksField = new JTextField();
    // 提交按钮使用JLabel作为成员变量，方便事件中访问
    JLabel submitLabel = new JLabel();
    JLabel returnLabel = new JLabel();
    JLabel importExcelLabel = new JLabel(); // Excel导入按钮
    JLabel downloadTemplateLabel = new JLabel(); // 下载模板按钮

    // 保存当前用户名
    private String currentUser;
    private ClassLoader classLoader;
    private String expenseType; // 当前开销类型
    private String templateFileName; // 当前模板文件名

    public WaterElectricityJFrame(String username) {
        this.currentUser = username;
        this.expenseType = "水电"; // 设置当前开销类型
        this.templateFileName = "WaterElectricity.xls"; // 设置当前模板文件名
        this.classLoader = WaterElectricityJFrame.class.getClassLoader();
        initJFrame();
        initView();
        this.setVisible(true);
    }

    private void initJFrame() {
        this.setSize(488, 430);
        this.setTitle("水电费用记录");
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 添加关闭操作
    }

    private void initView() {
        // 金额标签和输入框
        addField("金额", amountField, 0, 135);

        // 支付方法标签和输入框
        addField("支付方法", paymentMethodField, 0, 175);

        // 支付时间标签和输入框
        addField("支付时间", timeField, 0, 215);

        // 备注标签和输入框
        addField("备注", remarksField, 0, 255);

        // 提交按钮设置
        submitLabel.setBounds(60, 300, 80, 40);
        submitLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/提交.png")));
        submitLabel.setBorder(null);
        submitLabel.addMouseListener(this);
        this.getContentPane().add(submitLabel);

        // 返回按钮设置
        returnLabel.setBounds(150, 300, 80, 40);
        returnLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/返回.png")));
        returnLabel.setBorder(null);
        returnLabel.addMouseListener(this);
        this.getContentPane().add(returnLabel);

        // Excel导入按钮设置
        importExcelLabel.setBounds(240, 300, 80, 40);
        importExcelLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/导入Excel.png")));
        importExcelLabel.setBorder(null);
        importExcelLabel.addMouseListener(this);
        this.getContentPane().add(importExcelLabel);

        // 下载模板按钮设置
        downloadTemplateLabel.setBounds(330, 300, 80, 40);
        downloadTemplateLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/下载模板.png")));
        downloadTemplateLabel.setBorder(null);
        downloadTemplateLabel.addMouseListener(this);
        this.getContentPane().add(downloadTemplateLabel);

        // 添加背景
        JLabel background = new JLabel(new ImageIcon(classLoader.getResource("background/水电费用界面背景.png")));
        background.setBounds(0, 0, 470, 390);
        this.getContentPane().add(background);
    }

    // 通用字段添加方法
    private void addField(String fieldName, JTextField textField, int x, int y) {
        JLabel label = new JLabel(new ImageIcon(classLoader.getResource("Record_expenses/" + fieldName + ".png")));
        label.setBounds(x, y, 150, 50);
        this.getContentPane().add(label);

        textField.setBounds(140, y + 15, 200, 25);
        this.getContentPane().add(textField);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == submitLabel) {
            System.out.println("水电费用提交按钮被点击");
            saveExpenseData();
        } else if (e.getSource() == importExcelLabel) {
            System.out.println("导入Excel按钮被点击");
            importExcelData();
        } else if (e.getSource() == downloadTemplateLabel) {
            System.out.println("下载模板按钮被点击");
            downloadTemplate();
        }
        // 注意：返回按钮的页面跳转逻辑移到mousePressed中执行
    }

    // 鼠标按下时切换图片并执行页面跳转
    @Override
    public void mousePressed(MouseEvent e) {
        handleButtonPress(e, true);

        // 只有在鼠标按下时才执行页面跳转
        if (e.getSource() == returnLabel) {
            this.dispose();
            new Record_expensesJFrame(currentUser);
        }
    }

    // 鼠标松开时恢复图片
    @Override
    public void mouseReleased(MouseEvent e) {
        handleButtonPress(e, false);
    }

    // 鼠标进入时切换图片
    @Override
    public void mouseEntered(MouseEvent e) {
        handleButtonPress(e, true);
    }

    // 鼠标离开时恢复图片
    @Override
    public void mouseExited(MouseEvent e) {
        handleButtonPress(e, false);
    }

    // 统一处理按钮图片切换
    private void handleButtonPress(MouseEvent e, boolean isPressed) {
        String buttonName = getButtonName(e.getSource());
        if (!buttonName.isEmpty()) {
            String suffix = isPressed ? "按下" : "";
            String imagePath = "Record_expenses/" + buttonName + suffix + ".png";

            try {
                ImageIcon icon = new ImageIcon(classLoader.getResource(imagePath));
                ((JLabel) e.getSource()).setIcon(icon);
            } catch (Exception ex) {
                System.out.println("图片资源未找到: " + imagePath);
            }
        }
    }

    private String getButtonName(Object source) {
        if (source == submitLabel) return "提交";
        if (source == returnLabel) return "返回";
        if (source == importExcelLabel) return "导入Excel";
        if (source == downloadTemplateLabel) return "下载模板";
        return "";
    }

    // 使用Expense封装类保存数据
    private void saveExpenseData() {
        try {
            String amountText = amountField.getText().trim();
            String paymentMethod = paymentMethodField.getText().trim();
            String time = timeField.getText().trim();
            String remarks = remarksField.getText().trim();

            // 输入验证
            if (!validateInput(amountText, paymentMethod, time)) {
                return;
            }

            double amount = Double.parseDouble(amountText);

            // 创建Expense对象
            Expense expense = new Expense();
            expense.setAmount(amount);
            expense.setExpenseType(expenseType);
            expense.setPaymentMethod(paymentMethod);
            expense.setTime(java.sql.Date.valueOf(time)); // 假设输入格式为 yyyy-MM-dd
            expense.setRemarks(remarks);
            expense.setUserName(currentUser);

            // 保存到数据库
            if (saveExpenseToDatabase(expense)) {
                JOptionPane.showMessageDialog(this, "水电费用记录成功！");
                clearFields(); // 清空输入框
            } else {
                JOptionPane.showMessageDialog(this, "记录失败！");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "记录失败：" + ex.getMessage());
        }
    }

    // 验证输入数据
    private boolean validateInput(String amountText, String paymentMethod, String time) {
        if (amountText.isEmpty() || paymentMethod.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写必要信息！");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "金额必须大于0！");
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "金额格式错误！");
            return false;
        }

        // 验证日期格式
        if (!isValidDateFormat(time)) {
            JOptionPane.showMessageDialog(this, "时间格式应为 yyyy-MM-dd！");
            return false;
        }

        return true;
    }

    // 验证日期格式
    private boolean isValidDateFormat(String date) {
        try {
            java.sql.Date.valueOf(date);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // 保存Expense对象到数据库
    private boolean saveExpenseToDatabase(Expense expense) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO t_expense (amount, expense_type, payment_method, consume_time, remark, username) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setBigDecimal(1, java.math.BigDecimal.valueOf(expense.getAmount()));
            pstmt.setString(2, expense.getExpenseType());
            pstmt.setString(3, expense.getPaymentMethod());
            pstmt.setDate(4, new java.sql.Date(expense.getTime().getTime()));
            pstmt.setString(5, expense.getRemarks());
            pstmt.setString(6, expense.getUserName());

            return pstmt.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库操作失败：" + ex.getMessage());
            return false;
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
    }

    // 清空输入框
    private void clearFields() {
        amountField.setText("");
        paymentMethodField.setText("");
        timeField.setText("");
        remarksField.setText("");
    }

    // 导入Excel数据
    private void importExcelData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择开销Excel文件");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();

            try {
                // 使用现有的ExcelReaderUtil读取数据
                ExcelReaderUtil excelReader = new ExcelReaderUtil();
                List<Expense> expenseList = excelReader.readExcel(filePath);
                Map<Integer, String> errorMap = excelReader.getErrorMsgMap();

                // 处理解析错误
                if (!errorMap.isEmpty()) {
                    StringBuilder errorMsg = new StringBuilder("Excel解析失败：\n");
                    for (Map.Entry<Integer, String> entry : errorMap.entrySet()) {
                        if (entry.getKey() == -1) {
                            errorMsg.append(entry.getValue()).append("\n");
                        } else {
                            errorMsg.append("第").append(entry.getKey()).append("行：").append(entry.getValue()).append("\n");
                        }
                    }
                    JOptionPane.showMessageDialog(this, errorMsg.toString(), "解析失败", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 过滤出当前类型的开销数据
                List<Expense> filteredExpenses = expenseList.stream()
                        .filter(expense -> expense.getExpenseType().contains(expenseType))
                        .peek(expense -> expense.setUserName(currentUser)) // 设置当前用户名
                        .toList();

                if (filteredExpenses.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Excel中没有找到" + expenseType + "类型的开销数据！");
                    return;
                }

                // 批量插入数据库
                int successCount = DBUtil.batchInsertExpenses(filteredExpenses);
                JOptionPane.showMessageDialog(this,
                        "导入成功！共插入 " + successCount + " 条" + expenseType + "开销数据",
                        "导入成功", JOptionPane.INFORMATION_MESSAGE);

                // 清空当前输入框
                clearFields();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Excel导入失败：" + ex.getMessage(),
                        "导入失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 下载模板
    private void downloadTemplate() {
        try {
            // 使用ReadExcelData类的下载模板功能
            ReadExcelData.downloadTemplate(templateFileName, this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "模板下载失败：" + ex.getMessage(),
                    "下载失败", JOptionPane.ERROR_MESSAGE);
        }
    }
}
