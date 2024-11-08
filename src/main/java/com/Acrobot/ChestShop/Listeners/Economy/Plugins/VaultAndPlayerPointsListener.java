package com.Acrobot.ChestShop.Listeners.Economy.Plugins;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.Economy.*;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.black_ixx.playerpoints.util.PointsUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.logging.Level;

/**
 * Represents a Vault connector
 *
 * @author Acrobot
 */
public class VaultAndPlayerPointsListener extends EconomyAdapter {

    private Economy vault;
    private final PlayerPointsAPI playerPoints;

    public static @NotNull VaultAndPlayerPointsListener initialize() {
        return new VaultAndPlayerPointsListener();
    }

    private VaultAndPlayerPointsListener() {
        this.vault = loadEconomyProvider();
        this.playerPoints = PlayerPoints.getInstance().getAPI();
    }

    private Economy loadEconomyProvider() {
        RegisteredServiceProvider<Economy> provider = ChestShop.getBukkitServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            final String error = "No economy provider found. Please install a Vault-compatible economy plugin.";
            ChestShop.getBukkitLogger().severe(error);
            Bukkit.getPluginManager().disablePlugin(ChestShop.getPlugin());
            throw new IllegalStateException(error);
        }

        return provider.getProvider();
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getProvider() instanceof Economy) {
            this.vault = loadEconomyProvider();
        }
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (event.getProvider().getProvider() instanceof Economy) {
            this.vault = loadEconomyProvider();
        }
    }

    @EventHandler
    public void onAmountCheck(CurrencyAmountEvent event) {
        if (event.wasHandled() || !event.getAmount().equals(BigDecimal.ZERO)) {
            return;
        }

        final double balance;
        switch (event.getCurrencyType()) {
            case VAULT: {
                balance = vault.getBalance(Bukkit.getOfflinePlayer(event.getAccount()), event.getWorld().getName());
                break;
            }

            case PLAYER_POINTS: {
                balance = playerPoints.look(event.getAccount());
                break;
            }

            default:
                throw new UnsupportedOperationException(event.getCurrencyType().name());
        }

        event.setAmount(BigDecimal.valueOf(balance));
        event.setHandled(true);
    }

    @EventHandler
    public void onCurrencyCheck(CurrencyCheckEvent event) {
        if (event.wasHandled() || event.hasEnough()) {
            return;
        }

        final boolean result;
        switch (event.getCurrencyType()) {
            case VAULT: {
                result = vault.has(Bukkit.getOfflinePlayer(event.getAccount()), event.getWorld().getName(), event.getAmount().doubleValue());
                break;
            }

            case PLAYER_POINTS: {
                result = playerPoints.look(event.getAccount()) >= event.getAmount().doubleValue();
                break;
            }

            default:
                throw new UnsupportedOperationException(event.getCurrencyType().name());
        }

        event.hasEnough(result);
        event.setHandled(true);
    }

    @EventHandler
    public void onAccountCheck(AccountCheckEvent event) {
        if (event.wasHandled() || event.hasAccount()) {
            return;
        }

        final OfflinePlayer lastSeen = Bukkit.getOfflinePlayer(event.getAccount());

        try {
            event.hasAccount(vault.hasAccount(lastSeen, event.getWorld().getName()));
            event.setHandled(true);
        } catch (Exception e) {
            ChestShop.getBukkitLogger().log(Level.WARNING, "Could not check account balance of " + lastSeen.getUniqueId() + "/" + lastSeen.getName() + "." +
                    "This is probably due to https://github.com/MilkBowl/Vault/issues/746 and has to be fixed in either Vault directly or your economy plugin." +
                    "If you are sure it's not this issue then please report the following error.", e);
        }
    }

    @EventHandler
    public void onCurrencyFormat(CurrencyFormatEvent event) {
        if (event.wasHandled() || !event.getFormattedAmount().isEmpty()) {
            return;
        }

        final String formatted;
        switch (event.getCurrencyType()) {
            case VAULT: {
                formatted = vault.format(event.getAmount().doubleValue());
                break;
            }

            case PLAYER_POINTS: {
                formatted = PointsUtils.formatPoints(event.getAmount().longValueExact());
                break;
            }

            default:
                throw new UnsupportedOperationException(event.getCurrencyType().name());
        }

        event.setFormattedAmount(Properties.STRIP_PRICE_COLORS ? ChatColor.stripColor(formatted) : formatted);
        event.setHandled(true);
    }

    @EventHandler
    public void onCurrencyAdd(CurrencyAddEvent event) {
        if (event.wasHandled()) {
            return;
        }

        final boolean result;
        switch (event.getCurrencyType()) {
            case VAULT: {
                result = vault.depositPlayer(Bukkit.getOfflinePlayer(event.getTarget()), event.getWorld().getName(), event.getAmount().doubleValue()).transactionSuccess();
                break;
            }

            case PLAYER_POINTS: {
                result = playerPoints.give(event.getTarget(), event.getAmount().intValueExact());
                break;
            }

            default:
                throw new UnsupportedOperationException(event.getCurrencyType().name());
        }

        event.setHandled(result);
    }

    @EventHandler
    public void onCurrencySubtraction(CurrencySubtractEvent event) {
        if (event.wasHandled()) return;

        final boolean result;
        switch (event.getCurrencyType()) {
            case VAULT: {
                result = vault.withdrawPlayer(Bukkit.getOfflinePlayer(event.getTarget()), event.getWorld().getName(), event.getAmount().doubleValue()).transactionSuccess();
                break;
            }

            case PLAYER_POINTS: {
                result = playerPoints.take(event.getTarget(), event.getAmount().intValueExact());
                break;
            }

            default:
                throw new UnsupportedOperationException(event.getCurrencyType().name());
        }

        event.setHandled(result);
    }

    @EventHandler
    public void onCurrencyTransfer(CurrencyTransferEvent event) {
        processTransfer(event);
    }

    @EventHandler
    public void onCurrencyHoldCheck(CurrencyHoldEvent event) {
        if (event.wasHandled() || event.getAccount() == null || event.canHold()) return;

        switch (event.getCurrencyType()) {
            case VAULT: {
                final OfflinePlayer player = Bukkit.getOfflinePlayer(event.getAccount());
                final double amount = event.getAmount().doubleValue();

                if (!vault.depositPlayer(player, amount).transactionSuccess()) {
                    event.setHandled(true);
                    event.canHold(false);
                    return;
                }

                EconomyResponse response = vault.withdrawPlayer(player, amount);
                if (!response.transactionSuccess()) ChestShop.getBukkitLogger().severe(response.errorMessage);

                event.setHandled(true);
                event.canHold(true);
                return;
            }

            case PLAYER_POINTS: {
                final int amount = event.getAmount().intValueExact();

                if (!playerPoints.give(event.getAccount(), amount)) {
                    event.setHandled(true);
                    event.canHold(false);
                    return;
                }

                playerPoints.take(event.getAccount(), amount);

                event.setHandled(true);
                event.canHold(true);
                return;
            }

            default:
                throw new UnsupportedOperationException(event.getCurrencyType().name());
        }
    }
}
