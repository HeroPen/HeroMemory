package com.vault.gui;

import com.vault.config.AppConfig;
import com.vault.entity.Category;
import com.vault.entity.PasswordEntry;
import com.vault.service.VaultService;
import com.vault.util.BackupManager;
import com.vault.util.ClipboardUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainFrame extends JFrame {

    private final VaultService vaultService;

    private JTree categoryTree;
    private JTable entryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton deleteAllButton;
    private JButton copyUserButton;
    private JButton copyPassButton;
    private JButton backupButton;
    private JButton restoreButton;

    private String currentCategory = null;

    private static final String[] COLUMN_NAMES = {"标题", "用户名", "网址", "备注"};

    public MainFrame(VaultService vaultService) {
        super(AppConfig.APP_NAME);
        this.vaultService = vaultService;
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(200);

        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);

        JPanel leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel("HeroMemory");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("搜索:");
        searchLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("分类"));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("全部分类");
        DefaultMutableTreeNode allNode = new DefaultMutableTreeNode("全部");
        root.add(allNode);
        for (Category c : Category.values()) {
            root.add(new DefaultMutableTreeNode(c.getDisplayName()));
        }

        categoryTree = new JTree(new DefaultTreeModel(root));
        categoryTree.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        categoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        categoryTree.setRootVisible(false);
        categoryTree.setShowsRootHandles(true);

        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    categoryTree.getLastSelectedPathComponent();
            if (node == null) return;
            String label = node.getUserObject().toString();
            if ("全部".equals(label)) {
                currentCategory = null;
            } else {
                currentCategory = label;
            }
            refreshTable();
        });

        categoryTree.setSelectionRow(0);

        JScrollPane scrollPane = new JScrollPane(categoryTree);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        entryTable = new JTable(tableModel);
        entryTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        entryTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        entryTable.setRowHeight(25);
        entryTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entryTable.getTableHeader().setReorderingAllowed(false);

        entryTable.getSelectionModel().addListSelectionListener(this::onTableSelectionChanged);

        JScrollPane scrollPane = new JScrollPane(entryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("新增");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        deleteAllButton = new JButton("全部删除");
        copyUserButton = new JButton("复制用户名");
        copyPassButton = new JButton("复制密码");

        Font btnFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        addButton.setFont(btnFont);
        editButton.setFont(btnFont);
        deleteButton.setFont(btnFont);
        deleteAllButton.setFont(btnFont);
        copyUserButton.setFont(btnFont);
        copyPassButton.setFont(btnFont);

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        copyUserButton.setEnabled(false);
        copyPassButton.setEnabled(false);

        addButton.addActionListener(e -> showEntryDialog(null));
        editButton.addActionListener(e -> {
            PasswordEntry entry = getSelectedEntry();
            if (entry != null) {
                showEntryDialog(entry);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedEntry());
        deleteAllButton.addActionListener(e -> deleteAllEntries());
        copyUserButton.addActionListener(e -> copySelectedUsername());
        copyPassButton.addActionListener(e -> copySelectedPassword());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllButton);
        buttonPanel.add(copyUserButton);
        buttonPanel.add(copyPassButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        backupButton = new JButton("备份");
        restoreButton = new JButton("恢复");

        Font btnFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        backupButton.setFont(btnFont);
        restoreButton.setFont(btnFont);

        backupButton.addActionListener(e -> backup());
        restoreButton.addActionListener(e -> restore());

        panel.add(backupButton);
        panel.add(restoreButton);

        return panel;
    }

    private void onTableSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        boolean hasSelection = entryTable.getSelectedRow() >= 0;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        copyUserButton.setEnabled(hasSelection);
        copyPassButton.setEnabled(hasSelection);
    }

    private PasswordEntry getSelectedEntry() {
        int row = entryTable.getSelectedRow();
        if (row < 0) return null;
        List<PasswordEntry> data = getCurrentData();
        if (row < data.size()) {
            return data.get(row);
        }
        return null;
    }

    private List<PasswordEntry> getCurrentData() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            if (currentCategory == null) {
                return vaultService.getEntries();
            } else {
                return vaultService.getEntriesByCategory(currentCategory);
            }
        } else {
            if (currentCategory == null) {
                return vaultService.search(keyword);
            } else {
                return vaultService.search(keyword).stream()
                        .filter(e -> currentCategory.equals(e.getCategory()))
                        .collect(java.util.stream.Collectors.toList());
            }
        }
    }

    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        List<PasswordEntry> data = getCurrentData();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (PasswordEntry entry : data) {
            tableModel.addRow(new Object[]{
                    entry.getTitle(),
                    entry.getUsername(),
                    entry.getUrl(),
                    entry.getNote()
            });
        }
    }

    private void search() {
        refreshTable();
    }

    private void showEntryDialog(PasswordEntry existing) {
        boolean isNew = (existing == null);
        JDialog dialog = new JDialog(this, isNew ? "新增条目" : "编辑条目", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 13);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("标题:");
        titleLabel.setFont(labelFont);
        panel.add(titleLabel, gbc);
        JTextField titleField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel urlLabel = new JLabel("网址:");
        urlLabel.setFont(labelFont);
        panel.add(urlLabel, gbc);
        JTextField urlField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(urlField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(labelFont);
        panel.add(userLabel, gbc);
        JTextField userField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(userField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel("密码:");
        passLabel.setFont(labelFont);
        panel.add(passLabel, gbc);
        JPasswordField passField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        JLabel noteLabel = new JLabel("备注:");
        noteLabel.setFont(labelFont);
        panel.add(noteLabel, gbc);
        JTextField noteField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(noteField, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        JLabel catLabel = new JLabel("分类:");
        catLabel.setFont(labelFont);
        panel.add(catLabel, gbc);
        JComboBox<String> catCombo = new JComboBox<>();
        for (Category c : Category.values()) {
            catCombo.addItem(c.getDisplayName());
        }
        gbc.gridx = 1;
        panel.add(catCombo, gbc);

        if (!isNew) {
            titleField.setText(existing.getTitle());
            urlField.setText(existing.getUrl());
            userField.setText(existing.getUsername());
            passField.setText(existing.getPassword());
            noteField.setText(existing.getNote());
            catCombo.setSelectedItem(existing.getCategory());
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        saveButton.setFont(labelFont);
        cancelButton.setFont(labelFont);

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "标题不能为空");
                return;
            }
            if (isNew) {
                PasswordEntry entry = new PasswordEntry();
                entry.setTitle(title);
                entry.setUrl(urlField.getText().trim());
                entry.setUsername(userField.getText().trim());
                entry.setPassword(new String(passField.getPassword()));
                entry.setNote(noteField.getText().trim());
                entry.setCategory((String) catCombo.getSelectedItem());
                vaultService.addEntry(entry);
            } else {
                existing.setTitle(title);
                existing.setUrl(urlField.getText().trim());
                existing.setUsername(userField.getText().trim());
                existing.setPassword(new String(passField.getPassword()));
                existing.setNote(noteField.getText().trim());
                existing.setCategory((String) catCombo.getSelectedItem());
                vaultService.updateEntry(existing);
            }
            refreshTable();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelectedEntry() {
        PasswordEntry entry = getSelectedEntry();
        if (entry == null) return;
        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除条目 \"" + entry.getTitle() + "\" 吗？",
                "确认删除", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            vaultService.deleteEntry(entry.getId());
            refreshTable();
        }
    }

    private void deleteAllEntries() {
        List<PasswordEntry> data = getCurrentData();
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可删除的条目");
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除当前列表中的所有 " + data.size() + " 个条目吗？\n此操作不可恢复！",
                "确认全部删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            for (PasswordEntry entry : data) {
                vaultService.deleteEntry(entry.getId());
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "已删除 " + data.size() + " 个条目");
        }
    }

    private void copySelectedUsername() {
        PasswordEntry entry = getSelectedEntry();
        if (entry != null && entry.getUsername() != null) {
            ClipboardUtil.copy(entry.getUsername());
            JOptionPane.showMessageDialog(this, "用户名已复制到剪贴板");
        }
    }

    private void copySelectedPassword() {
        PasswordEntry entry = getSelectedEntry();
        if (entry != null && entry.getPassword() != null) {
            ClipboardUtil.copy(entry.getPassword());
            JOptionPane.showMessageDialog(this, "密码已复制到剪贴板");
        }
    }

    private void backup() {
        try {
            BackupManager.backup();
            JOptionPane.showMessageDialog(this, "备份成功");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "备份失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restore() {
        String[] backups = BackupManager.listBackups();
        if (backups.length == 0) {
            JOptionPane.showMessageDialog(this, "没有找到备份文件");
            return;
        }
        String selected = (String) JOptionPane.showInputDialog(this,
                "选择要恢复的备份:", "恢复备份",
                JOptionPane.PLAIN_MESSAGE, null, backups, backups[0]);
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "恢复将覆盖当前所有数据，确定继续？",
                "确认恢复", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            vaultService.loadEntries();
            BackupManager.restore(selected);
            vaultService.loadEntries();
            refreshTable();
            JOptionPane.showMessageDialog(this, "恢复成功");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "恢复失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
