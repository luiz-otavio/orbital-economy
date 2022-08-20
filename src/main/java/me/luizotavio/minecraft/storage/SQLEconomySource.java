/*
 * MIT License
 *
 * Copyright (c) [2022] [LUIZ O. F. CORRÊA]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.luizotavio.minecraft.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.luizotavio.minecraft.pojo.EconomyPlayer;
import me.luizotavio.minecraft.pojo.transaction.PendingTransaction;
import me.luizotavio.minecraft.storage.queue.SQLEconomyQueue;
import me.luizotavio.minecraft.util.SQLReader;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class SQLEconomySource {

    private final ExecutorService sourceService;

    private final HikariDataSource dataSource;

    private final SQLReader sqlReader;

    public SQLEconomySource(@NotNull Properties properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("Properties cannot be empty");
        }

        String sqlDriver = properties.getProperty("economy.source_driver"),
            driver = switch (sqlDriver.toUpperCase(Locale.ROOT)) {
                case "MYSQL", "MARIADB" -> "org.mariadb.jdbc.MariaDbDataSource";
                case "POSTGRESQL" -> "org.postgresql.ds.PGSimpleDataSource";
                case "H2" -> "org.h2.jdbcx.JdbcDataSource";
                default -> throw new IllegalArgumentException("Invalid driver: " + sqlDriver);
            };

        properties.setProperty("dataSourceClassName", driver);

        dataSource = new HikariDataSource(
            new HikariConfig(properties)
        );

        sourceService = Executors.newFixedThreadPool(
            Integer.parseInt(
                properties.getProperty("economy.worker_threads", "1")
            )
        );

        sqlReader = new SQLReader();

        // Load from SQL sources
        try {
            sqlReader.loadFromResources();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        createEnvironment()
            .thenAccept(unused -> {
                Bukkit.getLogger().info("[SQLEconomy] Environment created");
            });
    }

    public void close() {
        dataSource.close();
    }

    protected CompletableFuture<Void> createEnvironment() {
        String query = sqlReader.getSql("create_economy_table");

        if (query == null) {
            throw new IllegalStateException("SQL query not found: create_economy_table");
        }

        return CompletableFuture.runAsync(() -> {
            if (ensureClosed()) {
                return;
            }

            try (var connection = dataSource.getConnection()) {
                connection.createStatement()
                    .execute(query);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, sourceService);
    }

    @NotNull
    public CompletableFuture<EconomyPlayer> retrieveByUUID(@NotNull UUID uniqueId, @NotNull String nickname) {
        String query = sqlReader.getSql("retrieve_economy_data");

        if (query == null) {
            throw new IllegalStateException("SQL query not found");
        }

        return CompletableFuture.supplyAsync(() -> {
            if (ensureClosed()) {
                return null;
            }

            try (var connection = dataSource.getConnection()) {
                var result = connection.prepareStatement(query)
                    .executeQuery();

                if (!result.next()) {
                    return new EconomyPlayer(uniqueId, nickname, null, null, null);
                }

                BigDecimal currency = result.getBigDecimal("currency");

                Instant createdAt = result.getTimestamp("created_at")
                    .toInstant(),
                    updatedAt = result.getTimestamp("updated_at")
                        .toInstant();

                return new EconomyPlayer(
                    uniqueId,
                    result.getString("nickname"),
                    currency,
                    createdAt,
                    updatedAt
                );
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, sourceService).thenApply(economyPlayer -> {
            if (ensureClosed() || economyPlayer.getNickname().equals(nickname)) {
                return economyPlayer;
            }

            try (var connection = dataSource.getConnection()) {
                var statement = connection.prepareStatement(sqlReader.getSql("update_economy_nickname"));

                statement.setString(1, nickname);
                statement.setString(2, uniqueId.toString());

                statement.execute();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            return economyPlayer;
        });
    }

    public CompletableFuture<Void> execute(@NotNull Collection<PendingTransaction> pendingTransactions) {
        String update = sqlReader.getSql("update_economy_currency");

        if (update == null) {
            throw new IllegalStateException("SQL query not found");
        }

        return CompletableFuture.runAsync(() -> {
            if (ensureClosed()) {
                return;
            }

            try (var connection = dataSource.getConnection()) {
                var statement = connection.prepareStatement(update);

                for (var pendingTransaction : pendingTransactions) {
                    statement.setBigDecimal(1, pendingTransaction.amount());

                    statement.setString(
                        2,
                        pendingTransaction.to().toString()
                    );

                    statement.addBatch();
                }

                int[] results = statement.executeBatch();

                if (results.length != pendingTransactions.size()) {
                    throw new IllegalStateException("Some transactions were not executed");
                }

                connection.commit();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, sourceService);
    }

    private boolean ensureClosed() {
        return dataSource.isClosed();
    }
}
