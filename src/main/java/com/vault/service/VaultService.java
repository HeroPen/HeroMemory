package com.vault.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.vault.config.AppConfig;
import com.vault.entity.PasswordEntry;
import com.vault.security.CryptoUtil;

import javax.crypto.SecretKey;
import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class VaultService {

    private final List<PasswordEntry> entries = new CopyOnWriteArrayList<>();
    private SecretKey secretKey;
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isFirstTime() {
        return !new File(AppConfig.SALT_FILE).exists();
    }

    public void setMasterPassword(String password) throws Exception {
        byte[] salt = CryptoUtil.generateSalt();
        saveSalt(salt);
        String hash = CryptoUtil.hashMasterPassword(password, salt);
        saveMasterPasswordHash(hash);
        this.secretKey = CryptoUtil.deriveKey(password, salt);
        this.initialized = true;
        saveEntries();
    }

    public boolean verifyMasterPassword(String password) throws Exception {
        byte[] salt = loadSalt();
        String storedHash = loadMasterPasswordHash();
        String hash = CryptoUtil.hashMasterPassword(password, salt);
        if (!storedHash.equals(hash)) {
            return false;
        }
        this.secretKey = CryptoUtil.deriveKey(password, salt);
        this.initialized = true;
        loadEntries();
        return true;
    }

    public List<PasswordEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public void addEntry(PasswordEntry entry) {
        entry.setId(UUID.randomUUID().toString().replace("-", ""));
        long now = System.currentTimeMillis();
        entry.setCreateTime(now);
        entry.setUpdateTime(now);
        entries.add(entry);
        saveEntries();
    }

    public void updateEntry(PasswordEntry entry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(entry.getId())) {
                entry.setUpdateTime(System.currentTimeMillis());
                entries.set(i, entry);
                break;
            }
        }
        saveEntries();
    }

    public void deleteEntry(String id) {
        entries.removeIf(e -> e.getId().equals(id));
        saveEntries();
    }

    public PasswordEntry getById(String id) {
        return entries.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<PasswordEntry> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getEntries();
        }
        String lower = keyword.toLowerCase();
        return entries.stream()
                .filter(e -> (e.getTitle() != null && e.getTitle().toLowerCase().contains(lower))
                        || (e.getUrl() != null && e.getUrl().toLowerCase().contains(lower))
                        || (e.getUsername() != null && e.getUsername().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    public List<PasswordEntry> getEntriesByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return getEntries();
        }
        return entries.stream()
                .filter(e -> category.equals(e.getCategory()))
                .collect(Collectors.toList());
    }

    public void loadEntries() {
        File dataFile = new File(AppConfig.DATA_FILE);
        if (!dataFile.exists()) {
            entries.clear();
            return;
        }
        try {
            String encryptedJson = readFile(AppConfig.DATA_FILE);
            if (encryptedJson == null || encryptedJson.trim().isEmpty()) {
                entries.clear();
                return;
            }
            String json = CryptoUtil.decrypt(encryptedJson, secretKey);
            List<PasswordEntry> loaded = JSON.parseObject(json, new TypeReference<List<PasswordEntry>>() {});
            entries.clear();
            if (loaded != null) {
                entries.addAll(loaded);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load entries: " + e.getMessage(), e);
        }
    }

    public void saveEntries() {
        try {
            File dir = new File(AppConfig.VAULT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String json = JSON.toJSONString(entries);
            String encrypted = CryptoUtil.encrypt(json, secretKey);
            writeFile(AppConfig.DATA_FILE, encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save entries: " + e.getMessage(), e);
        }
    }

    private void saveSalt(byte[] salt) throws IOException {
        File dir = new File(AppConfig.VAULT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        writeBytes(AppConfig.SALT_FILE, salt);
    }

    private byte[] loadSalt() throws IOException {
        return readBytes(AppConfig.SALT_FILE);
    }

    private void saveMasterPasswordHash(String hash) throws IOException {
        writeFile(AppConfig.VAULT_DIR + File.separator + "master.hash", hash);
    }

    private String loadMasterPasswordHash() throws IOException {
        return readFile(AppConfig.VAULT_DIR + File.separator + "master.hash");
    }

    private static void writeFile(String path, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), "UTF-8"))) {
            writer.write(content);
        }
    }

    private static String readFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static void writeBytes(String path, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }

    private static byte[] readBytes(String path) throws IOException {
        File file = new File(path);
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }
        return data;
    }
}
