package com.mecanicacassoni.connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private static final Properties PROPERTIES = new Properties();

        static {
            try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("database.properties")) {

                if (input == null) {
                    throw new RuntimeException("Arquivo database.properties não encontrado!");
                }

                PROPERTIES.load(input);

            } catch (IOException e) {
                throw new RuntimeException("Erro ao carregar configurações do banco!", e);
            }
        }

        public static Connection getConnection() throws SQLException {
            String url = PROPERTIES.getProperty("db.url");
            String user = PROPERTIES.getProperty("db.user");
            String password = PROPERTIES.getProperty("db.password");

            return DriverManager.getConnection(url, user, password);
        }
}
