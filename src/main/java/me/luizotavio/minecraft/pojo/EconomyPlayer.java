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

package me.luizotavio.minecraft.pojo;

import me.luizotavio.minecraft.event.impl.EconomySubtractCurrencyEvent;
import me.luizotavio.minecraft.event.impl.EconomySumCurrencyEvent;
import me.luizotavio.minecraft.event.impl.EconomyUpdateCurrencyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class EconomyPlayer {

    private final UUID uniqueId;
    private final String nickname;

    private BigDecimal currency;

    private final Instant createdAt;
    private final Instant updatedAt;

    public EconomyPlayer(
        @NotNull UUID uniqueId,
        @NotNull String nickname,
        @Nullable BigDecimal currency,
        @Nullable Instant createdAt,
        @Nullable Instant updatedAt
    ) {
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.currency = currency == null ? BigDecimal.valueOf(0) : currency;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getNickname() {
        return nickname;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public BigDecimal getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void sum(@NotNull BigDecimal amount) {
        Player player = getPlayer();

        if (player == null) {
            return;
        }

        EconomySumCurrencyEvent event = new EconomySumCurrencyEvent(
            player,
            this,
            currency.add(amount)
        ).call();

        if (event.isCancelled()) {
            return;
        }

        currency = event.getNewCurrency();
    }

    public void subtract(@NotNull BigDecimal amount) {
        Player player = getPlayer();

        if (player == null) {
            return;
        }

        EconomySubtractCurrencyEvent event = new EconomySumCurrencyEvent(
            player,
            this,
            currency.subtract(amount)
        ).call();

        if (event.isCancelled()) {
            return;
        }

        currency = event.getNewCurrency();
    }

    public void setCurrency(@NotNull BigDecimal currency) {
        Player player = getPlayer();

        if (player == null) {
            return;
        }

        EconomyUpdateCurrencyEvent event = new EconomyUpdateCurrencyEvent(
            player,
            this,
            currency
        ).call();

        if (event.isCancelled()) {
            return;
        }

        this.currency = event.getNewCurrency();
    }
}
