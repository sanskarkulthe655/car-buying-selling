package com.autoadmin;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfiguration {
    private static final String MYSQL_SCHEME = "mysql://";
    private static final String JDBC_MYSQL_SCHEME = "jdbc:mysql://";

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String configuredUrl = properties.getUrl();
        if (configuredUrl == null || configuredUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL is required. Configure a MySQL connection before starting AutoAdmin.");
        }
        if (!configuredUrl.startsWith(MYSQL_SCHEME) && !configuredUrl.startsWith(JDBC_MYSQL_SCHEME)) {
            throw new IllegalArgumentException("AutoAdmin supports MySQL only. DATABASE_URL must use mysql:// or jdbc:mysql://.");
        }
        if (configuredUrl.startsWith(MYSQL_SCHEME)) {
            String credentials = rawUserInfo(configuredUrl);
            properties.setUrl(jdbcUrl(configuredUrl));
            if (credentials != null) {
                String[] parts = credentials.split(":", 2);
                properties.setUsername(decode(parts[0]));
                if (parts.length > 1) {
                    properties.setPassword(decode(parts[1]));
                }
            }
        }
        return properties.initializeDataSourceBuilder().build();
    }

    static String jdbcUrl(String configuredUrl) {
        if (configuredUrl == null || configuredUrl.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL is required.");
        }
        if (configuredUrl.startsWith(JDBC_MYSQL_SCHEME)) {
            return configuredUrl;
        }
        if (!configuredUrl.startsWith(MYSQL_SCHEME)) {
            throw new IllegalArgumentException("AutoAdmin supports MySQL only. Use mysql:// or jdbc:mysql://.");
        }

        String dsn = configuredUrl.substring(MYSQL_SCHEME.length());
        int queryStart = dsn.indexOf('?');
        String addressAndPath = queryStart < 0 ? dsn : dsn.substring(0, queryStart);
        String rawQuery = queryStart < 0 ? "" : dsn.substring(queryStart + 1);
        URI address = URI.create(MYSQL_SCHEME + addressAndPath);
        if (address.getHost() == null) {
            throw new IllegalArgumentException("DATABASE_URL must include a MySQL host.");
        }

        StringBuilder jdbcUrl = new StringBuilder(JDBC_MYSQL_SCHEME).append(address.getHost());
        if (address.getPort() >= 0) {
            jdbcUrl.append(':').append(address.getPort());
        }
        if (address.getRawPath() != null) {
            jdbcUrl.append(address.getRawPath());
        }

        List<String> jdbcOptions = new ArrayList<>();
        boolean hasTlsOption = false;
        boolean verifyTlsIdentity = false;
        if (!rawQuery.isBlank()) {
            for (String option : rawQuery.split("&")) {
                String key = option.split("=", 2)[0].toLowerCase(Locale.ROOT);
                if (key.equals("ssl")) {
                    // Node-style ssl={...} options are not Connector/J properties.
                    verifyTlsIdentity = option.toLowerCase(Locale.ROOT).contains("rejectunauthorized\":true");
                    continue;
                }
                if (key.equals("sslmode") || key.equals("usessl")) {
                    hasTlsOption = true;
                }
                if (!option.isBlank()) {
                    jdbcOptions.add(option);
                }
            }
        }
        if (!hasTlsOption) {
            jdbcOptions.add(verifyTlsIdentity ? "sslMode=VERIFY_IDENTITY" : "sslMode=REQUIRED");
        }
        if (!jdbcOptions.isEmpty()) {
            jdbcUrl.append('?').append(String.join("&", jdbcOptions));
        }
        return jdbcUrl.toString();
    }

    private static String rawUserInfo(String dsn) {
        int schemeEnd = MYSQL_SCHEME.length();
        int pathStart = dsn.indexOf('/', schemeEnd);
        int queryStart = dsn.indexOf('?', schemeEnd);
        int authorityEnd = dsn.length();
        if (pathStart >= 0) authorityEnd = Math.min(authorityEnd, pathStart);
        if (queryStart >= 0) authorityEnd = Math.min(authorityEnd, queryStart);
        int at = dsn.lastIndexOf('@', authorityEnd);
        return at < schemeEnd ? null : dsn.substring(schemeEnd, at);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
