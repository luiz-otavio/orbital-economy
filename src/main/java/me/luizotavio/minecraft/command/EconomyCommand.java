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
import me.luizotavio.minecraft.pojo.EconomyPlayer;
import me.luizotavio.minecraft.pojo.transaction.PendingTransaction;
import me.luizotavio.minecraft.storage.queue.SQLEconomyQueue;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.annotation.Optional;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

import static me.luizotavio.minecraft.util.Colors.translate;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class EconomyCommand {

    private final SQLEconomyQueue economyQueue;
    private final EconomyController economyController;

    public EconomyCommand(
        @NotNull SQLEconomyQueue economyQueue,
        @NotNull EconomyController economyController
    ) {
        this.economyQueue = economyQueue;
        this.economyController = economyController;
    }

    @Command(
        name = "balance",
        target = CommandTarget.PLAYER,
        aliases = {"bal"}
    )
    public void handleBalanceCommand(Context<Player> context, @Optional String target) {
        Player player = context.getSender();

        if (target != null) {
            Player targetPlayer = Bukkit.getPlayerExact(target);

            if (targetPlayer == null) {
                player.sendMessage(translate("&cPlayer not found."));
                return;
            }

            EconomyPlayer economyPlayer = economyController.get(targetPlayer.getUniqueId());

            if (economyPlayer == null) {
                player.sendMessage(translate("&cPlayer not found."));
                return;
            }

            player.sendMessage(translate("&aBalance of &6%s&a: &6%s", targetPlayer.getName(), economyPlayer.getCurrency().toPlainString()));
            return;
        }

        EconomyPlayer economyPlayer = economyController.get(player.getUniqueId());

        if (economyPlayer == null) {
            player.sendMessage(
                translate("&cYou don't have any money yet. Use &6/earn &cto earn some!")
            );

            return;
        }

        player.sendMessage(
            translate("&aYour balance is &6${0}", economyPlayer.getCurrency().toPlainString())
        );
    }

    @Command(
        name = "give",
        target = CommandTarget.PLAYER,
        aliases = {"ecogive"}
    )
    public void handleGiveCommand(Context<Player> context, @Optional String target, @Optional String amount) {
        Player player = context.getSender();

        EconomyPlayer economyPlayer = economyController.get(player.getUniqueId());

        if (economyPlayer == null) {
            player.sendMessage(
                translate("&cYou don't have any money yet. Use &6/earn &cto earn some!")
            );

            return;
        }

        Player targetPlayer = Bukkit.getPlayerExact(target);

        if (targetPlayer == null) {
            player.sendMessage(translate("&cPlayer not found."));
            return;
        }

        EconomyPlayer targetEconomyPlayer = economyController.get(targetPlayer.getUniqueId());

        if (targetEconomyPlayer == null) {
            player.sendMessage(translate("&cPlayer not found."));
            return;
        }

        if (amount == null) {
            player.sendMessage(translate("&cYou must specify an amount."));
            return;
        }

        if (!NumberUtils.isNumber(amount)) {
            player.sendMessage(translate("&cInvalid amount."));
            return;
        }

        BigDecimal amountBigDecimal = new BigDecimal(amount);

        if (amountBigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage(translate("&cInvalid amount."));
            return;
        }

        if (amountBigDecimal.compareTo(economyPlayer.getCurrency()) > 0) {
            player.sendMessage(translate("&cYou don't have enough money."));
            return;
        }

        economyPlayer.subtract(amountBigDecimal);
        targetEconomyPlayer.sum(amountBigDecimal);

        PendingTransaction pendingTransaction = PendingTransaction.now(
            player.getUniqueId(),
            economyPlayer.getCurrency()
        ), targetPendingTransaction = PendingTransaction.now(
            targetPlayer.getUniqueId(),
            targetEconomyPlayer.getCurrency()
        );

        economyQueue.queue(pendingTransaction);
        economyQueue.queue(targetPendingTransaction);

        player.sendMessage(
            translate("&aYou gave &6${0} &ato &6${1}", targetPlayer.getName(), amountBigDecimal.toPlainString())
        );
    }

    @Command(
        name = "setbalance",
        target = CommandTarget.PLAYER,
        aliases = {"setbal"}
    )
    public void handleSetBalanceCommand(Context<Player> context, @Optional String target, @Optional String amount) {
        Player player = context.getSender();

        if (!player.isOp()) {
            player.sendMessage(translate("&cYou don't have permission to do this."));
            return;
        }

        Player targetPlayer = Bukkit.getPlayerExact(target);

        if (targetPlayer == null) {
            player.sendMessage(translate("&cPlayer not found."));
            return;
        }

        EconomyPlayer economyPlayer = economyController.get(targetPlayer.getUniqueId());

        if (economyPlayer == null) {
            player.sendMessage(
                translate("&cYou don't have any money yet. Use &6/earn &cto earn some!")
            );

            return;
        }

        if (amount == null) {
            player.sendMessage(translate("&cYou must specify an amount."));
            return;
        }

        if (!NumberUtils.isNumber(amount)) {
            player.sendMessage(translate("&cInvalid amount."));
            return;
        }

        BigDecimal amountBigDecimal = new BigDecimal(amount);

        if (amountBigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage(translate("&cInvalid amount."));
            return;
        }

        economyPlayer.setCurrency(amountBigDecimal);

        PendingTransaction pendingTransaction = PendingTransaction.now(
            player.getUniqueId(),
            economyPlayer.getCurrency()
        );

        economyQueue.queue(pendingTransaction);

        player.sendMessage(
            translate("&aYou set the balance of &6${0} &ato &6${1}", targetPlayer.getName(), amountBigDecimal.toPlainString())
        );
    }
}
