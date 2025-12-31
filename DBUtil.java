package org.example.util;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库操作工具类
 */
public class DBUtil {
    // 数据库连接配置（请替换为实际数据库信息）
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/daily_expenses?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "253342344";

    static {
        // 加载数据库驱动
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("数据库驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接对象
     * @throws SQLException 数据库连接异常
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * 批量插入开销数据
     * @param expenseList 开销数据列表
     * @return 成功插入的记录数
     * @throws SQLException 数据库操作异常
     */
    public static int batchInsertExpenses(List<Expense> expenseList) throws SQLException {
        if (expenseList == null || expenseList.isEmpty()) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        int totalCount = 0;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 按照开销类型分组处理
            Map<String, List<Expense>> groupedExpenses = expenseList.stream()
                    .collect(Collectors.groupingBy(Expense::getExpenseType));

            for (Map.Entry<String, List<Expense>> entry : groupedExpenses.entrySet()) {
                String expenseType = entry.getKey();
                List<Expense> expenses = entry.getValue();

                // 根据开销类型确定表名
                String tableName = getTableNameByExpenseType(expenseType);
                if (tableName == null) {
                    throw new SQLException("不支持的开销类型: " + expenseType);
                }

                // 构建SQL语句
                String sql = buildInsertSQL(tableName, expenseType);
                pstmt = conn.prepareStatement(sql);

                for (Expense expense : expenses) {
                    setPreparedStatementParams(pstmt, expense, expenseType);
                    pstmt.addBatch();
                }

                int[] results = pstmt.executeBatch();
                totalCount += results.length;
                pstmt.close();
            }

            conn.commit(); // 提交事务
            return totalCount;

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback(); // 回滚事务
            }
            throw new SQLException("批量插入失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            closeResources(conn, pstmt, null);
        }
    }

    // 根据开销类型获取表名
    private static String getTableNameByExpenseType(String expenseType) {
        switch (expenseType) {
            case "出行":
                return "travel_options";
            case "通讯":
                return "communication_expenses";
            case "水电":
                return "water_electricity_bills";
            case "食品餐饮":
                return "food_beverage";
            case "娱乐":
                return "entertainment";
            case "生活日用品":
                return "daily_necessities";
            case "其他":
                return "other_expenses";
            default:
                return null;
        }
    }

    // 构建插入SQL语句（根据您的实际表结构调整）
    private static String buildInsertSQL(String tableName, String expenseType) {
        // 假设水电表结构不包含expense_type字段，其他表包含
        if ("水电".equals(expenseType)) {
            return "INSERT INTO " + tableName + " (amount, payment_method, time, remarks, User_name) VALUES (?, ?, ?, ?, ?)";
        } else {
            return "INSERT INTO " + tableName + " (amount, expense_type, payment_method, time, remarks, User_name) VALUES (?, ?, ?, ?, ?, ?)";
        }
    }

    // 设置PreparedStatement参数（根据您的实际表结构调整）
    private static void setPreparedStatementParams(PreparedStatement pstmt, Expense expense, String expenseType) throws SQLException {
        if ("水电".equals(expenseType)) {
            // 水电费用表结构：amount, payment_method, time, remarks, User_name
            pstmt.setBigDecimal(1, BigDecimal.valueOf(expense.getAmount()));
            pstmt.setString(2, expense.getPaymentMethod());
            pstmt.setDate(3, new java.sql.Date(expense.getTime().getTime()));
            pstmt.setString(4, expense.getRemarks());
            pstmt.setString(5, expense.getUserName());
        } else {
            // 其他表结构：amount, expense_type, payment_method, time, remarks, User_name
            pstmt.setBigDecimal(1, BigDecimal.valueOf(expense.getAmount()));
            pstmt.setString(2, expense.getExpenseType());
            pstmt.setString(3, expense.getPaymentMethod());
            pstmt.setDate(4, new java.sql.Date(expense.getTime().getTime()));
            pstmt.setString(5, expense.getRemarks());
            pstmt.setString(6, expense.getUserName());
        }
    }

    /**
     * 关闭数据库资源通用方法
     * @param conn 数据库连接
     * @param stmt 语句对象
     * @param rs 结果集
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
