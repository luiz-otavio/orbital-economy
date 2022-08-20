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

package me.luizotavio.minecraft;

import me.luizotavio.minecraft.command.EarnCommand;
import me.luizotavio.minecraft.command.EconomyCommand;
import me.luizotavio.minecraft.controller.EconomyController;
import me.luizotavio.minecraft.listener.EconomyHandler;
import me.luizotavio.minecraft.storage.queue.SQLEconomyQueue;
import me.luizotavio.minecraft.storage.SQLEconomySource;
import me.saiintbrisson.bukkit.command.BukkitFrame;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 19/08/2022
 */
public class EconomyPlugin extends JavaPlugin {

    @NotNull
    public static EconomyPlugin getInstance() {
        return getPlugin(EconomyPlugin.class);
    }

    private EconomyController economyController;

    private SQLEconomySource economySource;
    private SQLEconomyQueue economyQueue;

    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            saveResource("database.properties", false);
        }

        Properties properties = new Properties();

        try {
            properties.load(
                Files.newInputStream(
                    new File(getDataFolder(), "database.properties").toPath()
                )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        economySource = new SQLEconomySource(properties);
        economyQueue = new SQLEconomyQueue(economySource);
    }

    @Override
    public void onEnable() {
        economyController = new EconomyController();

        economyQueue.initialize(this);

        BukkitFrame bukkitFrame = new BukkitFrame(this);

        bukkitFrame.registerCommands(
            new EarnCommand(economyQueue, economyController),
            new EconomyCommand(economyQueue, economyController)
        );

        getServer().getPluginManager()
            .registerEvents(new EconomyHandler(economySource, economyController), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        economySource.close();

        Bukkit.getScheduler().cancelTasks(this);

    }

    public EconomyController getEconomyController() {
        return economyController;
    }

    public SQLEconomyQueue getEconomyQueue() {
        return economyQueue;
    }

    public SQLEconomySource getEconomySource() {
        return economySource;
    }
}
