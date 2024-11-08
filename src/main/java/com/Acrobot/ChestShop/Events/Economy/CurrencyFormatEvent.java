package com.Acrobot.ChestShop.Events.Economy;

import com.Acrobot.ChestShop.CurrencyType;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

/**
 * Represents a moment when the currency needs to be shown
 *
 * @author Acrobot
 */
public class CurrencyFormatEvent extends EconomicEvent {
    private static final HandlerList handlers = new HandlerList();

    private final BigDecimal amount;
    private final CurrencyType currencyType;
    private String formattedAmount = "";

    public CurrencyFormatEvent(BigDecimal amount, CurrencyType currencyType) {
        this.amount = amount;
        this.currencyType = currencyType;
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
     * @return Already formatted currency amount
     */
    public String getFormattedAmount() {
        return formattedAmount;
    }

    /**
     * Sets the currency formatting
     *
     * @param formattedAmount Formatted amount
     */
    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
