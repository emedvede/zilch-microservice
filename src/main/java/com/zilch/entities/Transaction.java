package com.zilch.entities;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 *  Transaction entity.
 *
 *  @author Elena Medvedeva
 */
@Entity
@Table(name = "transaction")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Transaction globalId must not be empty")
    @NotNull(message = "Transaction globalId must be provided")
    @Column(name = "global_id", unique = true, nullable = false)
    private String globalId;

    @NotNull(message = "Transaction typeId must be provided")
    @ManyToOne
    @JoinColumn(name = "type_id")
    private TransactionType type;

    @NotNull(message = "Transaction amount must be provided")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Transaction card must be provided")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @Column(name = "submitted")
    private Boolean submitted;

    @Column(name = "due_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @NotNull(message = "Transaction currency must be provided")
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

    public Transaction(){ }

    public Transaction( String globalId, TransactionType type, BigDecimal amount,  Card card, Purchase purchase, Currency currency, String description, boolean submitted, Date dueDate) {
        this.globalId = globalId;
        this.type = type;
        this.amount = amount;
        this.card = card;
        this.currency = currency;
        this.submitted = submitted;
        this.purchase = purchase;
        this.dueDate = dueDate;
        this.description = description;
        this.lastUpdated = new Date();
    }

    public Transaction( String globalId, TransactionType type, BigDecimal amount,  Card card, Purchase purchase, Currency currency, String description) {
        this(globalId,type,amount,card,purchase, currency, description, true, new Date());
    }

    public Transaction( String globalId, TransactionType type, BigDecimal amount,  Card card, Purchase purchase, Currency currency, String description, String lastUpdatedBy) {
       this(globalId,type,amount,card,purchase, currency, description);
       this.lastUpdatedBy = lastUpdatedBy;
    }

    public Transaction( String globalId, TransactionType type, BigDecimal amount,  Card card, Purchase purchase, Currency currency, String description,boolean submitted, Date dueDate, String lastUpdatedBy) {
        this(globalId,type,amount,card,purchase, currency, description, submitted, dueDate);
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
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

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        dueDate = dueDate;
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

}
