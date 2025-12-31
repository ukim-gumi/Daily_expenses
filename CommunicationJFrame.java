package org.example.ui;

import org.example.util.DBUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CommunicationJFrame extends JFrame implements MouseListener {

    JTextField amountField = new JTextField();
    JTextField paymentMethodField = new JTextField();
    JTextField timeField = new JTextField();
    JTextField remarksField = new JTextField();
    // 提交按钮使用JLabel作为成员变量，方便事件中访问
    JLabel submitLabel = new JLabel();
    JLabel returnLabel = new JLabel();

    // 保存当前用户名
    private String currentUser;

    public CommunicationJFrame(String username) {
        this.currentUser = username;
        initJFrame();
        initView();
        this.setVisible(true);
    }

    private void initJFrame() {
        this.setSize(488, 430);
        this.setTitle("通讯费用记录");
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 添加关闭操作
    }

    private void initView() {
        // 通过类加载器获取资源（使用当前类的类加载器）
        ClassLoader classLoader = WaterElectricityJFrame.class.getClassLoader();

        // 金额标签和输入框
        JLabel amountLabel = new JLabel(new ImageIcon(classLoader.getResource("Record_expenses/金额.png")));
        amountLabel.setBounds(0, 135, 150, 50);
        this.getContentPane().add(amountLabel);

        amountField.setBounds(140, 150, 200, 25);
        this.getContentPane().add(amountField);

        // 支付方法标签和输入框
        JLabel paymentMethodLabel = new JLabel(new ImageIcon(classLoader.getResource("Record_expenses/支付方法.png")));
        paymentMethodLabel.setBounds(0, 175, 150, 50);
        this.getContentPane().add(paymentMethodLabel);

        paymentMethodField.setBounds(140, 190, 200, 25);
        this.getContentPane().add(paymentMethodField);

        // 支付时间标签和输入框
        JLabel timeLabel = new JLabel(new ImageIcon(classLoader.getResource("Record_expenses/支付时间.png")));
        timeLabel.setBounds(0, 215, 150, 50);
        this.getContentPane().add(timeLabel);

        timeField.setBounds(140, 230, 200, 25);
        this.getContentPane().add(timeField);

        // 备注标签和输入框
        JLabel remarksLabel = new JLabel(new ImageIcon(classLoader.getResource("Record_expenses/备注.png")));
        remarksLabel.setBounds(0, 255, 150, 50);
        this.getContentPane().add(remarksLabel);

        remarksField.setBounds(140, 270, 200, 25);
        this.getContentPane().add(remarksField);

        // 提交按钮设置
        submitLabel.setBounds(100, 300, 150, 50);
        submitLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/提交.png")));
        // 去除边框和背景
        submitLabel.setBorder(null);
        // 绑定鼠标事件
        submitLabel.addMouseListener(this);
        this.getContentPane().add(submitLabel);

        // 返回按钮设置
        returnLabel.setBounds(250, 300, 150, 50);
        returnLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/返回.png")));
        // 去除边框和背景
        returnLabel.setBorder(null);
        // 绑定鼠标事件
        returnLabel.addMouseListener(this);
        this.getContentPane().add(returnLabel);

        // 添加背景
        JLabel background = new JLabel(new ImageIcon(classLoader.getResource("background/通讯费用界面背景.png")));
        background.setBounds(0, 0, 470, 390);
        this.getContentPane().add(background);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // 点击提交按钮时执行数据库操作
        if (e.getSource() == submitLabel) {
            System.out.println("水电费用提交按钮被点击");
            saveWaterElectricityData();
        }
    }

    // 鼠标按下时切换图片
    @Override
    public void mousePressed(MouseEvent e) {
        ClassLoader classLoader = WaterElectricityJFrame.class.getClassLoader();
        if (e.getSource() == submitLabel) {
            submitLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/提交按下.png")));
        }
        if (e.getSource() == returnLabel) {
            returnLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/返回按下.png")));
            this.dispose(); // 关闭当前界面
            new Record_expensesJFrame(currentUser);
        }
    }

    // 鼠标松开时恢复图片
    @Override
    public void mouseReleased(MouseEvent e) {
        ClassLoader classLoader = WaterElectricityJFrame.class.getClassLoader();
        if (e.getSource() == submitLabel) {
            submitLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/提交.png")));
        }
        if (e.getSource() == returnLabel) {
            returnLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/返回.png")));
        }
    }

    // 鼠标进入时切换图片
    @Override
    public void mouseEntered(MouseEvent e) {
        ClassLoader classLoader = WaterElectricityJFrame.class.getClassLoader();
        if (e.getSource() == submitLabel) {
            submitLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/提交按下.png")));
        }
        if (e.getSource() == returnLabel) {
            returnLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/返回按下.png")));
        }
    }

    // 鼠标离开时恢复图片
    @Override
    public void mouseExited(MouseEvent e) {
        ClassLoader classLoader = WaterElectricityJFrame.class.getClassLoader();
        if (e.getSource() == submitLabel) {
            submitLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/提交.png")));
        }
        if (e.getSource() == returnLabel) {
            returnLabel.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/返回.png")));
        }
    }

    private void saveWaterElectricityData() {
        try {
            String amountText = amountField.getText().trim();
            String paymentMethod = paymentMethodField.getText().trim();
            String time = timeField.getText().trim();
            String remarks = remarksField.getText().trim();

            // 输入验证
            if (amountText.isEmpty() || paymentMethod.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写必要信息！");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "金额格式错误！");
                return;
            }

            Connection conn = DBUtil.getConnection();
            String sql = "INSERT INTO water_electricity_bills (Amount, payment_method, time, remarks, User_name) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, paymentMethod);
            pstmt.setString(3, time);
            pstmt.setString(4, remarks);
            pstmt.setString(5, currentUser);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "通讯费用记录成功！");
                this.dispose(); // 关闭窗口
            }

            // 释放资源
            pstmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "记录失败：" + ex.getMessage());
        }
    }
}