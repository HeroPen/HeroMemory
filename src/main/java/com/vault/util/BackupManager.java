package com.vault.util;

import com.vault.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager {

    public static void backup() throws IOException {
        File dataFile = new File(AppConfig.DATA_FILE);
        if (!dataFile.exists()) {
            throw new IOException("No data file to backup.");
        }

        File backupDir = new File(AppConfig.BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupName = "vault_backup_" + timestamp + ".dat";
        Path backupPath = new File(backupDir, backupName).toPath();

        Files.copy(dataFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void restore(String backupFileName) throws IOException {
        File backupFile = new File(AppConfig.BACKUP_DIR, backupFileName);
        if (!backupFile.exists()) {
            throw new IOException("Backup file not found: " + backupFileName);
        }

        Files.copy(backupFile.toPath(), new File(AppConfig.DATA_FILE).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public static String[] listBackups() {
        File backupDir = new File(AppConfig.BACKUP_DIR);
        if (!backupDir.exists()) {
            return new String[0];
        }
        String[] files = backupDir.list((dir, name) -> name.startsWith("vault_backup_") && name.endsWith(".dat"));
        return files != null ? files : new String[0];
    }
}
