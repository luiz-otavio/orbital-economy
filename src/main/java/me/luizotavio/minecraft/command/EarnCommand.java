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

package me.luizotavio.minecraft.command;

import me.luizotavio.minecraft.controller.EconomyController;
import me.luizotavio.minecraft.pojo.transaction.PendingTransaction;
import me.luizotavio.minecraft.storage.queue.SQLEconomyQueue;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class EarnCommand {

    // Since this cooldown is just available for that command, we don't need to store it in a specific pattern.
    private static final Map<UUID, Long> COOLDOWN_MAP = new Hashtable<>();

    private final SQLEconomyQueue economyQueue;
    private final EconomyController economyController;

    public EarnCommand(
        @NotNull SQLEconomyQueue economyQueue,
        @NotNull EconomyController economyController
    ) {
        this.economyQueue = economyQueue;
        this.economyController = economyController;
    }

    @Command(
        name = "earn",
        target = CommandTarget.PLAYER,
        aliases = {"gain"}
    )
    public void handleEarnCommand(Context<Player> context) {
        Player player = context.getSender();

        long currentTime = System.currentTimeMillis();

        if (COOLDOWN_MAP.containsKey(player.getUniqueId()) && COOLDOWN_MAP.get(player.getUniqueId()) > currentTime) {
            player.sendMessage("You can't use this command for another " + (COOLDOWN_MAP.get(player.getUniqueId()) - currentTime) / 1000 + " seconds.");
            return;
        }

        COOLDOWN_MAP.put(player.getUniqueId(), currentTime + TimeUnit.MINUTES.toMillis(1));

        int range = ThreadLocalRandom.current()
            .nextInt(1, 5);

        economyQueue.queue(
            PendingTransaction.now(player.getUniqueId(), BigDecimal.valueOf(range))
        );

        player.sendMessage("You have earned " + range + " coins!");
    }

}
