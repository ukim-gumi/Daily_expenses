package org.example.ui;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Record_expensesJFrame extends JFrame implements MouseListener {

    JButton entertainmentBtn = new JButton();
    JButton waterElectricityBtn = new JButton();
    JButton foodBeverageBtn = new JButton();
    JButton travelBtn = new JButton();
    JButton dailyNecessitiesBtn = new JButton();
    JButton communicationBtn = new JButton();
    JButton otherBtn = new JButton();

    // 使用Map存储按钮和对应的Frame类
    private Map<JButton, Class<? extends JFrame>> buttonFrameMap = new HashMap<>();

    // 保存当前用户名
    private String currentUser;
    // 类加载器成员变量（用于全局访问资源）
    private ClassLoader classLoader;

    public Record_expensesJFrame(String username) {
        this.currentUser = username;
        //初始化界面
        initJFrame();
        //在这个界面中添加内容
        initView();
        //让当前界面显示出来
        this.setVisible(true);
    }

    public void initView() {
        //初始化类加载器（成员变量）
        classLoader = Record_expensesJFrame.class.getClassLoader();

        // 建立按钮与Frame类的映射关系
        buttonFrameMap.put(entertainmentBtn, EntertainmentJFrame.class);
        buttonFrameMap.put(waterElectricityBtn, WaterElectricityJFrame.class);
        buttonFrameMap.put(foodBeverageBtn, FoodBeverageJFrame.class);
        buttonFrameMap.put(travelBtn, TravelJFrame.class);
        buttonFrameMap.put(dailyNecessitiesBtn, DailyNecessitiesJFrame.class);
        buttonFrameMap.put(communicationBtn, CommunicationJFrame.class);
        buttonFrameMap.put(otherBtn, OtherExpensesJFrame.class);

        // 娱乐按钮
        setupButton(entertainmentBtn, "娱乐", 70, 100);

        // 水电费用按钮
        setupButton(waterElectricityBtn, "水电费用", 270, 100);

        // 食品餐饮按钮
        setupButton(foodBeverageBtn, "食品餐饮", 70, 150);

        // 交通出行按钮
        setupButton(travelBtn, "交通出行", 270, 150);

        // 生活用品按钮
        setupButton(dailyNecessitiesBtn, "生活用品", 70, 200);

        // 通讯费用按钮
        setupButton(communicationBtn, "通讯费用", 270, 200);

        // 其他按钮
        setupButton(otherBtn, "其他", 170, 250);

        //添加背景
        JLabel background = new JLabel(new ImageIcon(classLoader.getResource("background/主界面背景.png")));
        background.setBounds(0, 0, 500, 400);
        this.getContentPane().add(background);
    }

    private void setupButton(JButton button, String buttonText, int x, int y) {
        button.setBounds(x, y, 150, 50);
        button.setIcon(new ImageIcon(classLoader.getResource("Record_expenses/" + buttonText + ".png")));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addMouseListener(this);
        this.getContentPane().add(button);
    }

    public void initJFrame() {
        this.setSize(510, 440);
        this.setTitle("每日花销 - 选择类别");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setLayout(null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Class<? extends JFrame> frameClass = buttonFrameMap.get(e.getSource());
        if (frameClass != null) {
            try {
                Constructor<? extends JFrame> constructor = frameClass.getConstructor(String.class);
                constructor.newInstance(currentUser);
                this.dispose(); // 关闭当前界面
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "打开界面失败：" + ex.getMessage());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // 保持空实现（按需求不需要处理按下事件）
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // 保持空实现（按需求不需要处理释放事件）
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        handleButtonHover(e, true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        handleButtonHover(e, false);
    }

    private void handleButtonHover(MouseEvent e, boolean isEntered) {
        JButton sourceButton = (JButton) e.getSource();
        String buttonText = getButtonText(sourceButton);

        if (!buttonText.isEmpty()) {
            String imageSuffix = isEntered ? "按下" : "";
            String imagePath = "Record_expenses/" + buttonText + imageSuffix + ".png";
            sourceButton.setIcon(new ImageIcon(classLoader.getResource(imagePath)));
        }
    }

    private String getButtonText(JButton button) {
        if (button == entertainmentBtn) return "娱乐";
        if (button == waterElectricityBtn) return "水电费用";
        if (button == foodBeverageBtn) return "食品餐饮";
        if (button == travelBtn) return "交通出行";
        if (button == dailyNecessitiesBtn) return "生活用品";
        if (button == communicationBtn) return "通讯费用";
        if (button == otherBtn) return "其他";
        return "";
    }
}
