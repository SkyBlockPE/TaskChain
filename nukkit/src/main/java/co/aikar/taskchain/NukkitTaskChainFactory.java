/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.taskchain;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.nukkit.Nukkit;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.plugin.PluginDisableEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

@SuppressWarnings({"unused"})
public class NukkitTaskChainFactory extends TaskChainFactory {
    private NukkitTaskChainFactory(PluginBase plugin, AsyncQueue asyncQueue) {
        super(new NukkitGameInterface(plugin, asyncQueue));
    }

    public static TaskChainFactory create(PluginBase plugin) {
        return new NukkitTaskChainFactory(plugin, new TaskChainAsyncQueue());
    }
/* @TODO: #9 - Not Safe to do this
    public static TaskChainFactory create(Plugin plugin, ThreadPoolExecutor executor) {
        return new BukkitTaskChainFactory(plugin, new TaskChainAsyncQueue(executor));
    }

    public static TaskChainFactory create(Plugin plugin, AsyncQueue asyncQueue) {
        return new BukkitTaskChainFactory(plugin, asyncQueue);
    }*/

    private static class NukkitGameInterface implements GameInterface {
        private final PluginBase plugin;
        private final AsyncQueue asyncQueue;

        NukkitGameInterface(PluginBase plugin, AsyncQueue asyncQueue) {
            this.plugin = plugin;
            this.asyncQueue = asyncQueue;
        }

        @Override
        public AsyncQueue getAsyncQueue() {
            return this.asyncQueue;
        }

        @Override
        public boolean isMainThread() {
            return Server.getInstance().isPrimaryThread();
        }

        @Override
        public void postToMain(Runnable run) {
        	Server.getInstance().getScheduler().scheduleTask(plugin, run);
        }

        @Override
        public void scheduleTask(int ticks, Runnable run) {
        	Server.getInstance().getScheduler().scheduleDelayedTask(plugin, run, ticks);
        }

        @Override
        public void registerShutdownHandler(TaskChainFactory factory) {
        	Server.getInstance().getPluginManager().registerEvents(new Listener() {
        		@EventHandler
        		public void onPluginDisable(PluginDisableEvent event) {
        			if(event.getPlugin().equals(plugin)){
        				factory.shutdown(60, TimeUnit.SECONDS);
        			}
        		}
			}, plugin);
        }
    }

    public static final TaskChainAbortAction<Player, String, ?> MESSAGE = new TaskChainAbortAction<Player, String, Object>() {
        @Override
        public void onAbort(TaskChain<?> chain, Player player, String message) {
            player.sendMessage(message);
        }
    };
    public static final TaskChainAbortAction<Player, String, ?> COLOR_MESSAGE = new TaskChainAbortAction<Player, String, Object>() {
        @Override
        public void onAbort(TaskChain<?> chain, Player player, String message) {
            player.sendMessage(TextFormat.colorize('&', message));
        }
    };
}
