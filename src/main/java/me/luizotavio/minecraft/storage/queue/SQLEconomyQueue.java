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

package me.luizotavio.minecraft.storage.queue;

import me.luizotavio.minecraft.pojo.transaction.PendingTransaction;
import me.luizotavio.minecraft.storage.SQLEconomySource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class SQLEconomyQueue {

    private final Queue<PendingTransaction> transactions = new LinkedBlockingDeque<>();

    private final SQLEconomySource economySource;

    public SQLEconomyQueue(@NotNull SQLEconomySource economySource) {
        this.economySource = economySource;
    }

    public boolean queue(@NotNull PendingTransaction pendingTransaction) {
        return transactions.add(pendingTransaction);
    }

    @Nullable
    public PendingTransaction poll() {
        return transactions.poll();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    @NotNull
    public Collection<PendingTransaction> fillByPage(int page, int pageSize) {
        return transactions.stream()
            .skip((long) page * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());
    }

    public int initialize(@NotNull Plugin plugin) {
        BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

        String pageSize = System.getenv("ECONOMY_QUEUE_PAGE_SIZE");

        if (pageSize == null) {
            pageSize = System.getProperty("economy.queue.page.size", "48");
        }

        int size = Integer.parseInt(pageSize);

        return bukkitScheduler.runTaskTimerAsynchronously(plugin, () -> {
            if (isEmpty()) {
                return;
            }

            Collection<PendingTransaction> pendingTransactions = fillByPage(0, size);

            economySource.execute(pendingTransactions)
                .thenAccept(unused -> {
                    plugin.getLogger().info("Executed " + pendingTransactions.size() + " transactions");
                });
        }, 0L, 1L).getTaskId();
    }

}
