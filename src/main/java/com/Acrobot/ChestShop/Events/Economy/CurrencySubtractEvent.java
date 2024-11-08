package com.Acrobot.ChestShop.Events.Economy;

import com.Acrobot.ChestShop.CurrencyType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a subtraction of goods from entity
 *
 * Use {@link CurrencyTransferEvent} if you want to transfer money from one account to another one!
 *
 * @author Acrobot
 */
public class CurrencySubtractEvent extends EconomicEvent {
    private static final HandlerList handlers = new HandlerList();

    private BigDecimal amount;
    private UUID target;
    private World world;
    private final CurrencyType currencyType;

    public CurrencySubtractEvent(BigDecimal amount, UUID target, World world, CurrencyType currencyType) {
        this.amount = amount;
        this.target = target;
        this.world = world;
        this.currencyType = currencyType;
    }

    public CurrencySubtractEvent(BigDecimal amount, Player target, CurrencyType currencyType) {
        this(amount, target.getUniqueId(), target.getWorld(), currencyType);
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    /**
     * @return Amount of currency
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @return Amount of currency, as a double
     * @deprecated Use {@link #getAmount()} if possible
     */
    public double getDoubleAmount() {
        return amount.doubleValue();
    }

    /**
     * Sets the amount of currency transferred
     *
     * @param amount Amount to transfer
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Sets the amount of currency transferred
     *
     * @param amount Amount to transfer
     * @deprecated Use {@link #setAmount(java.math.BigDecimal)} if possible
     */
    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    /**
     * @return Was the money already subtracted?
     * @deprecated Use {@link #wasHandled()}
     */
    @Deprecated
    public boolean isSubtracted() {
        return wasHandled();
    }

    /**
     * Set if the money was subtracted from the account
     *
     * @param subtracted Was the money subtracted?
     * @deprecated Use {@link #setHandled(boolean)}
     */
    @Deprecated
    public void setSubtracted(boolean subtracted) {
        setHandled(subtracted);
    }

    /**
     * @return The world in which the transaction occurs
     */
    public World getWorld() {
        return world;
    }

    /**
     * @return Account from which the currency is subtracted
     */
    public UUID getTarget() {
        return target;
    }

    /**
     * @param target Account from which the currency is subtracted
     */
    public void setTarget(UUID target) {
        this.target = target;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
