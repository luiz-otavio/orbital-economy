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

package me.luizotavio.minecraft.controller;

import me.luizotavio.minecraft.pojo.EconomyPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class EconomyController {

    private final Map<UUID, EconomyPlayer> economyMap = new Hashtable<>();

    public void put(@NotNull UUID uuid, @NotNull EconomyPlayer economyPlayer) {
        economyMap.put(uuid, economyPlayer);
    }

    @Nullable
    public EconomyPlayer get(@NotNull UUID uuid) {
        return economyMap.get(uuid);
    }

    @Nullable
    public EconomyPlayer remove(@NotNull UUID uuid) {
        return economyMap.remove(uuid);
    }

    public boolean contains(@NotNull UUID uuid) {
        return economyMap.containsKey(uuid);
    }

    public void clear() {
        economyMap.clear();
    }



}
