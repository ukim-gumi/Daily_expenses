package org.example.util;

import java.util.Date;

/**
 * 开销实体类（对应数据库expenses表）
 */
public class Expense {
    private Double amount;        // 金额
    private String expenseType;   // 开销类型
    private String paymentMethod; // 支付方式
    private Date time;            // 消费时间
    private String remarks;       // 备注
    private String userName;      // 用户名

    // 空参构造
    public Expense() {}

    // 全参构造
    public Expense(Double amount, String expenseType, String paymentMethod, Date time, String remarks, String userName) {
        this.amount = amount;
        this.expenseType = expenseType;
        this.paymentMethod = paymentMethod;
        this.time = time;
        this.remarks = remarks;
        this.userName = userName;
    }

    // Getter & Setter
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}