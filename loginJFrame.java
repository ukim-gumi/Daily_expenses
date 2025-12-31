package org.example.ui;

import org.example.util.CodeUtil;
import org.example.util.DBUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class loginJFrame extends JFrame implements MouseListener {

    JButton login = new JButton();
    JButton register = new JButton();

    JTextField username = new JTextField();
    JPasswordField password = new JPasswordField();
    JTextField code = new JTextField();

    // 正确的验证码
    JLabel rightCode = new JLabel();

    public loginJFrame() {
        // 初始化界面
        initJFrame();
        // 在这个界面中添加内容
        initView();
        // 让当前界面显示出来
        this.setVisible(true);
    }

    public void initView() {
        // 通过类加载器获取资源，资源位于classpath下的login目录
        ClassLoader classLoader = loginJFrame.class.getClassLoader();

        // 添加用户名文字
        JLabel usernameText = new JLabel(new ImageIcon(classLoader.getResource("login/用户名.png")));
        usernameText.setBounds(116, 135, 47, 17);
        this.getContentPane().add(usernameText);

        // 添加用户名输入框
        username.setBounds(195, 134, 200, 30);
        this.getContentPane().add(username);

        // 添加密码文字
        JLabel passwordText = new JLabel(new ImageIcon(classLoader.getResource("login/密码.png")));
        passwordText.setBounds(130, 195, 32, 16);
        this.getContentPane().add(passwordText);

        // 密码输入框
        password.setBounds(195, 195, 200, 30);
        this.getContentPane().add(password);

        // 验证码提示
        JLabel codeText = new JLabel(new ImageIcon(classLoader.getResource("login/验证码.png")));
        codeText.setBounds(133, 256, 50, 30);
        this.getContentPane().add(codeText);

        // 验证码的输入框
        code.setBounds(195, 256, 100, 30);
        this.getContentPane().add(code);

        String codeStr = CodeUtil.getCode();
        // 设置内容
        rightCode.setText(codeStr);
        // 绑定鼠标事件
        rightCode.addMouseListener(this);
        // 位置和宽高
        rightCode.setBounds(300, 256, 50, 30);
        // 添加到界面
        this.getContentPane().add(rightCode);

        // 添加登录按钮
        login.setBounds(123, 310, 128, 47);
        login.setIcon(new ImageIcon(classLoader.getResource("login/登录按钮.png")));
        // 去除按钮的边框
        login.setBorderPainted(false);
        // 去除按钮的背景
        login.setContentAreaFilled(false);
        // 给登录按钮绑定鼠标事件
        login.addMouseListener(this);
        this.getContentPane().add(login);

        // 添加注册按钮
        register.setBounds(256, 310, 128, 47);
        register.setIcon(new ImageIcon(classLoader.getResource("login/注册按钮.png")));
        // 去除按钮的边框
        register.setBorderPainted(false);
        // 去除按钮的背景
        register.setContentAreaFilled(false);
        // 给注册按钮绑定鼠标事件
        register.addMouseListener(this);
        this.getContentPane().add(register);

        // 添加背景图片
        JLabel background = new JLabel(new ImageIcon(classLoader.getResource("background/主界面背景.png")));
        background.setBounds(0, 0, 470, 390);
        this.getContentPane().add(background);
    }

    public void initJFrame() {
        this.setSize(488, 430);// 设置宽高
        this.setTitle("每日花销 V1.0登录");// 设置标题
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);// 设置关闭模式
        this.setLocationRelativeTo(null);// 居中
        this.setAlwaysOnTop(true);// 置顶
        this.setLayout(null);// 取消内部默认布局
    }

    // 点击事件
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == login) {
            System.out.println("点击了登录按钮");
            // 获取两个文本输入框中的内容
            String usernameInput = username.getText();
            String passwordInput = new String(password.getPassword());
            // 获取用户输入的验证码
            String codeInput = code.getText();

            System.out.println("用户输入的用户名为" + usernameInput);
            System.out.println("用户输入的密码为" + passwordInput);

            if (codeInput.isEmpty()) {
                showJDialog("验证码不能为空");
            } else if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                // 校验用户名和密码是否为空
                System.out.println("用户名或者密码为空");
                showJDialog("用户名或者密码为空");
            } else if (!codeInput.equalsIgnoreCase(rightCode.getText())) {
                showJDialog("验证码输入错误");
            } else {
                // 连接数据库验证用户
                boolean isValidUser = validateUserFromDatabase(usernameInput, passwordInput);
                if (isValidUser) {
                    System.out.println("用户名和密码正确可以开始存储花销了");
                    // 关闭当前登录界面
                    this.setVisible(false);
                    // 打开记录花销的主界面，传递用户名
                    new Record_expensesJFrame(usernameInput);
                } else {
                    System.out.println("用户名或密码错误");
                    showJDialog("用户名或密码错误");
                }
            }
        } else if (e.getSource() == register) {
            System.out.println("点击了注册按钮");
            // 打开注册界面
            new resiterJFrame();
        } else if (e.getSource() == rightCode) {
            System.out.println("更换验证码");
            // 获取一个新的验证码
            String code = CodeUtil.getCode();
            rightCode.setText(code);
        }
    }


    private boolean validateUserFromDatabase(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();

            // 准备SQL语句，查询user表中的User_name和password字段
            String sql = "SELECT * FROM user WHERE User_name = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // 执行查询
            rs = pstmt.executeQuery();

            // 如果查询到记录，说明用户名和密码正确
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            showJDialog("数据库连接错误，请检查数据库配置");
            return false;
        } finally {
            // 关闭资源
            DBUtil.closeResources(conn, pstmt, null);
        }
    }

    public void showJDialog(String content) {
        // 创建一个弹框对象
        JDialog jDialog = new JDialog();
        // 给弹框设置大小
        jDialog.setSize(200, 150);
        // 让弹框置顶
        jDialog.setAlwaysOnTop(true);
        // 让弹框居中
        jDialog.setLocationRelativeTo(null);
        // 弹框不关闭永远无法操作下面的界面
        jDialog.setModal(true);

        // 创建Jlabel对象管理文字并添加到弹框当中
        JLabel warning = new JLabel(content);
        warning.setBounds(0, 0, 200, 150);
        jDialog.getContentPane().add(warning);

        // 让弹框展示出来
        jDialog.setVisible(true);
    }

    // 按下不松
    @Override
    public void mousePressed(MouseEvent e) {
        ClassLoader classLoader = loginJFrame.class.getClassLoader();
        if (e.getSource() == login) {
            login.setIcon(new ImageIcon(classLoader.getResource("login/登录按下.png")));
        } else if (e.getSource() == register) {
            register.setIcon(new ImageIcon(classLoader.getResource("login/注册按下.png")));
        }
    }

    // 松开按钮
    @Override
    public void mouseReleased(MouseEvent e) {
        ClassLoader classLoader = loginJFrame.class.getClassLoader();
        if (e.getSource() == login) {
            login.setIcon(new ImageIcon(classLoader.getResource("login/登录按钮.png")));
        } else if (e.getSource() == register) {
            register.setIcon(new ImageIcon(classLoader.getResource("login/注册按钮.png")));
        }
    }

    // 鼠标划入
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    // 鼠标划出
    @Override
    public void mouseExited(MouseEvent e) {
    }
}