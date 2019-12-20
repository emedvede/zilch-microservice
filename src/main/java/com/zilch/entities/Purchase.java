package com.zilch.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Purchase entity
 *
 * @author Elena Medvedeva
 */
@Entity
@Table(name = "purchase")
@EntityListeners(AuditingEntityListener.class)
public class Purchase {

    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Purchase globalId must not be empty")
    @NotNull(message = "Purchase globalId must be provided")
    @Column(name = "global_id", unique = true, nullable = false)
    private String globalId;

    @NotNull(message = "Shop Id must be provided")
    @Column(name = "shop_id")
    private String shopId;

    @Min(0)
    @NotNull(message = "Purchase amount must be provided")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Purchase card must be provided")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @NotNull(message = "Purchase currency must be provided")
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(name = "description")
    String description;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    public Purchase(){
    }

    public Purchase(String globalId, String shopId, Currency currency, BigDecimal amount,Card card, String description) {
        this.globalId = globalId;
        this.shopId = shopId;
        this.currency = currency;
        this.amount = amount;
        this.card = card;
        this.description = description;
        this.lastUpdated = new Date();
    }

    public Purchase(String globalId, String shopId, Currency currency, BigDecimal amount,Card card, String description, String lastUpdatedBy) {
        this(globalId,shopId,currency, amount, card, description);
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
