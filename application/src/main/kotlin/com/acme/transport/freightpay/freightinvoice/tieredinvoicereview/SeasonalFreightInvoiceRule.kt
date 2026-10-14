package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal freight invoice rule is in its life cycle. */
enum class SeasonalFreightInvoiceRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal freight invoice rule and its current status. */
data class SeasonalFreightInvoiceRule(
    var code: String,
    var name: String,
    var budget: Double,
    var discount: Double,
    var amount: Double,
    var notes: String,
    var shiftCode: String,
    var cost: Double,
    var createdAt: Int,
    var status: SeasonalFreightInvoiceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFreightInvoiceRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFreightInvoiceRuleStatus): Boolean {
        when (status) {
            SeasonalFreightInvoiceRuleStatus.IDLE -> return next == SeasonalFreightInvoiceRuleStatus.WARMING || next == SeasonalFreightInvoiceRuleStatus.STOPPED
            SeasonalFreightInvoiceRuleStatus.WARMING -> {
                when (next) {
                    SeasonalFreightInvoiceRuleStatus.READY, SeasonalFreightInvoiceRuleStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalFreightInvoiceRuleStatus.READY -> return next == SeasonalFreightInvoiceRuleStatus.DRAINING || next == SeasonalFreightInvoiceRuleStatus.STOPPED
            SeasonalFreightInvoiceRuleStatus.DRAINING -> {
                when (next) {
                    SeasonalFreightInvoiceRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalFreightInvoiceRuleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal freight invoice rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((discount - cost) * 100) / 100

    fun snapshot(): SeasonalFreightInvoiceRule {
        return SeasonalFreightInvoiceRule(
            code = code,
            name = name,
            budget = budget,
            discount = discount,
            amount = amount,
            notes = notes,
            shiftCode = shiftCode,
            cost = cost,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal freight invoice rule in status idle. */
fun newSeasonalFreightInvoiceRule(code: String, name: String): SeasonalFreightInvoiceRule {
    return SeasonalFreightInvoiceRule(
        code = code,
        name = name,
        budget = 0.0,
        discount = 0.0,
        amount = 0.0,
        notes = "",
        shiftCode = "",
        cost = 0.0,
        createdAt = 0,
        status = SeasonalFreightInvoiceRuleStatus.IDLE,
    )
}
