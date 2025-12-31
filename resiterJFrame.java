package org.example.ui;

import org.example.util.DBUtil;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class resiterJFrame extends JFrame implements MouseListener {

    JButton register = new JButton();
    JButton reset = new JButton();

    JTextField username = new JTextField();
    JPasswordField password = new JPasswordField();
    JPasswordField confirmPassword = new JPasswordField();

    public resiterJFrame() {
        // 初始化界面
        initJFrame();
        // 在这个界面中添加内容
        initView();
        // 让当前界面显示出来
        this.setVisible(true);
    }

    public void initView() {
        // 通过类加载器获取资源，资源位于classpath下的login目录
        ClassLoader classLoader = resiterJFrame.class.getClassLoader();

        // 添加用户名文字
        JLabel usernameText = new JLabel(new ImageIcon(classLoader.getResource("login/用户名.png")));
        usernameText.setBounds(85, 135, 47, 17);
        this.getContentPane().add(usernameText);

        // 添加用户名输入框
        username.setBounds(195, 134, 200, 30);
        this.getContentPane().add(username);

        // 添加密码文字
        JLabel passwordText = new JLabel(new ImageIcon(classLoader.getResource("login/密码.png")));
        passwordText.setBounds(97, 195, 32, 16);
        this.getContentPane().add(passwordText);

        // 密码输入框
        password.setBounds(195, 195, 200, 30);
        this.getContentPane().add(password);

        // 添加确认密码文字
        JLabel confirmPasswordText = new JLabel(new ImageIcon(classLoader.getResource("login/确认密码.png")));
        confirmPasswordText.setBounds(85, 255, 80, 30);
        this.getContentPane().add(confirmPasswordText);

        // 确认密码输入框
        confirmPassword.setBounds(195, 255, 200, 30);
        this.getContentPane().add(confirmPassword);

        // 添加注册按钮
        register.setBounds(120, 320, 128, 47);
        register.setIcon(new ImageIcon(classLoader.getResource("login/注册按钮.png")));
        // 去除按钮的边框
        register.setBorderPainted(false);
        // 去除按钮的背景
        register.setContentAreaFilled(false);
        // 给注册按钮绑定鼠标事件
        register.addMouseListener(this);
        this.getContentPane().add(register);

        // 添加重置按钮
        reset.setBounds(270, 320, 128, 47);
        reset.setIcon(new ImageIcon(classLoader.getResource("login/重置按钮.png")));
        // 去除按钮的边框
        reset.setBorderPainted(false);
        // 去除按钮的背景
        reset.setContentAreaFilled(false);
        reset.addMouseListener(this);
        this.getContentPane().add(reset);

        // 添加背景图片（使用与登录界面相同的背景）
        JLabel background = new JLabel(new ImageIcon(classLoader.getResource("login/主界面背景.png")));
        background.setBounds(0, 0, 470, 390);
        this.getContentPane().add(background);
    }

    public void initJFrame() {
        this.setSize(488, 430); // 设置宽高
        this.setTitle("每日花销 V1.0注册"); // 设置标题
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // 关闭注册窗口时不退出程序
        this.setLocationRelativeTo(null); // 居中
        this.setAlwaysOnTop(true); // 置顶
        this.setLayout(null); // 取消内部默认布局
    }

    // 点击事件处理
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == register) {
            System.out.println("点击了注册按钮");
            // 获取用户输入
            String usernameInput = username.getText();
            String passwordInput = new String(password.getPassword());
            String confirmPasswordInput = new String(confirmPassword.getPassword());

            // 验证输入
            if (usernameInput.isEmpty() || passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
                showJDialog("用户名和密码不能为空");
                return;
            }

            if (!passwordInput.equals(confirmPasswordInput)) {
                showJDialog("两次输入的密码不一致");
                return;
            }

            if (passwordInput.length() < 6) {
                showJDialog("密码长度不能少于6位");
                return;
            }

            // 注册用户到数据库
            boolean success = registerUserToDatabase(usernameInput, passwordInput);
            if (success) {
                showJDialog("注册成功！");
                // 注册成功后关闭注册窗口
                this.dispose();
            } else {
                showJDialog("注册失败，用户名可能已存在");
            }

        } else if (e.getSource() == reset) {
            System.out.println("点击了重置按钮");
            // 清空所有输入框
            username.setText("");
            password.setText("");
            confirmPassword.setText("");
        }
    }

    /**
     * 将用户注册信息存入数据库
     * @param username 用户名
     * @param password 密码
     * @return 注册是否成功
     */
    private boolean registerUserToDatabase(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();

            // 准备SQL语句，插入到user表的User_name和password字段
            String sql = "INSERT INTO user (User_name, password) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // 执行插入
            int rowsAffected = pstmt.executeUpdate();

            // 如果插入成功，返回true
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            // 用户名重复会抛出SQL异常，统一处理为注册失败
            return false;
        } finally {
            // 关闭资源
            DBUtil.closeResources(conn, pstmt, null);
        }
    }

    /**
     * 显示提示对话框
     * @param content 提示内容
     */
    public void showJDialog(String content) {
        JDialog jDialog = new JDialog();
        jDialog.setSize(200, 150);
        jDialog.setAlwaysOnTop(true);
        jDialog.setLocationRelativeTo(null);
        jDialog.setModal(true);

        JLabel warning = new JLabel(content);
        warning.setHorizontalAlignment(SwingConstants.CENTER);
        warning.setBounds(0, 0, 200, 150);
        jDialog.getContentPane().add(warning);

        jDialog.setVisible(true);
    }

    // 按下不松
    @Override
    public void mousePressed(MouseEvent e) {
        ClassLoader classLoader = resiterJFrame.class.getClassLoader();
        if (e.getSource() == register) {
            register.setIcon(new ImageIcon(classLoader.getResource("login/注册按下.png")));
        } else if (e.getSource() == reset) {
            reset.setIcon(new ImageIcon(classLoader.getResource("login/重置按钮按下.png")));
        }
    }

    // 松开按钮
    @Override
    public void mouseReleased(MouseEvent e) {
        ClassLoader classLoader = resiterJFrame.class.getClassLoader();
        if (e.getSource() == register) {
            register.setIcon(new ImageIcon(classLoader.getResource("login/注册按钮.png")));
        } else if (e.getSource() == reset) {
            reset.setIcon(new ImageIcon(classLoader.getResource("login/重置按钮.png")));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}