package com.vault;

import com.vault.gui.MainFrame;
import com.vault.gui.MasterPasswordDialog;
import com.vault.service.VaultService;

import javax.swing.*;

public class VaultMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        SwingUtilities.invokeLater(() -> {
            VaultService vaultService = new VaultService();

            MasterPasswordDialog dialog = new MasterPasswordDialog(null, vaultService);
            dialog.setVisible(true);

            if (dialog.isVerified()) {
                MainFrame mainFrame = new MainFrame(vaultService);
                mainFrame.setVisible(true);
            }
        });
    }
}
