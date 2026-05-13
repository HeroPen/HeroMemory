package com.vault.config;

import java.io.File;

public class AppConfig {

    public static final String APP_NAME = "HeroMemory";

    public static final String VAULT_DIR = System.getProperty("user.home")
            + File.separator + ".password-vault";

    public static final String DATA_FILE = VAULT_DIR + File.separator + "vault.dat";

    public static final String SALT_FILE = VAULT_DIR + File.separator + "salt.dat";

    public static final String BACKUP_DIR = VAULT_DIR + File.separator + "backup";

    public static final int PBKDF2_ITERATIONS = 100000;

    public static final int KEY_LENGTH = 128;

    public static final String ALGORITHM_AES = "AES/CBC/PKCS5Padding";

    public static final String ALGORITHM_PBKDF2 = "PBKDF2WithHmacSHA256";
}
