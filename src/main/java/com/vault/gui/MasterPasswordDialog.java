package com.vault.gui;

import com.vault.config.AppConfig;
import com.vault.service.VaultService;

import javax.swing.*;
import java.awt.*;

public class MasterPasswordDialog extends JDialog {

    private final VaultService vaultService;
    private boolean verified = false;

    public MasterPasswordDialog(JFrame parent, VaultService vaultService) {
        super(parent, AppConfig.APP_NAME, true);
        this.vaultService = vaultService;
        initUI();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        boolean firstTime = vaultService.isFirstTime();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel(firstTime ? "设置主密码" : "请输入主密码");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel messageLabel = new JLabel(firstTime
                ? "首次使用，请设置您的主密码（用于加密所有数据）"
                : "输入主密码解锁密码保险箱");
        messageLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        gbc.gridy = 1;
        panel.add(messageLabel, gbc);

        JLabel passwordLabel = new JLabel("主密码:");
        passwordLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JLabel confirmLabel = new JLabel("确认密码:");
        confirmLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        gbc.gridy = 3;
        gbc.gridx = 0;
        JPasswordField confirmField = new JPasswordField(20);
        gbc.gridx = 1;

        if (firstTime) {
            panel.add(confirmLabel, gbc);
            gbc.gridx = 1;
            panel.add(confirmField, gbc);
        }

        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(errorLabel, gbc);

        JButton okButton = new JButton("确定");
        okButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(okButton, gbc);

        JButton cancelButton = new JButton("退出");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(cancelButton, gbc);

        okButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (password.isEmpty()) {
                errorLabel.setText("密码不能为空");
                return;
            }
            if (firstTime) {
                String confirm = new String(confirmField.getPassword());
                if (!password.equals(confirm)) {
                    errorLabel.setText("两次输入的密码不一致");
                    return;
                }
                try {
                    vaultService.setMasterPassword(password);
                    verified = true;
                    dispose();
                } catch (Exception ex) {
                    errorLabel.setText("设置失败: " + ex.getMessage());
                }
            } else {
                try {
                    if (vaultService.verifyMasterPassword(password)) {
                        verified = true;
                        dispose();
                    } else {
                        errorLabel.setText("主密码错误");
                    }
                } catch (Exception ex) {
                    errorLabel.setText("验证失败: " + ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(e -> {
            System.exit(0);
        });

        passwordField.addActionListener(e -> okButton.doClick());

        add(panel);
        pack();
    }

    public boolean isVerified() {
        return verified;
    }
}
