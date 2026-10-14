package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy freight invoice rule is in its life cycle. */
enum class LegacyFreightInvoiceRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy freight invoice rule and its current status. */
data class LegacyFreightInvoiceRule(
    var code: String,
    var title: String,
    var limit: Int,
    var minimum: Double,
    var cost: Double,
    var weight: Double,
    var notes: String,
    var name: String,
    var status: LegacyFreightInvoiceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFreightInvoiceRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFreightInvoiceRuleStatus): Boolean {
        when (status) {
            LegacyFreightInvoiceRuleStatus.NEW -> return next == LegacyFreightInvoiceRuleStatus.QUEUED || next == LegacyFreightInvoiceRuleStatus.DONE
            LegacyFreightInvoiceRuleStatus.QUEUED -> return next == LegacyFreightInvoiceRuleStatus.RUNNING || next == LegacyFreightInvoiceRuleStatus.DONE
            LegacyFreightInvoiceRuleStatus.RUNNING -> {
                when (next) {
                    LegacyFreightInvoiceRuleStatus.FAILED -> return true
                }
                return false
            }
            LegacyFreightInvoiceRuleStatus.DONE -> return next == LegacyFreightInvoiceRuleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyFreightInvoiceRuleStatus) {
        if (!canMoveTo(next)) {
            error("legacy freight invoice rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((weight + cost) * 100) / 100

    fun snapshot(): LegacyFreightInvoiceRule {
        return LegacyFreightInvoiceRule(
            code = code,
            title = title,
            limit = limit,
            minimum = minimum,
            cost = cost,
            weight = weight,
            notes = notes,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy freight invoice rule in status new. */
fun newLegacyFreightInvoiceRule(code: String, title: String): LegacyFreightInvoiceRule {
    return LegacyFreightInvoiceRule(
        code = code,
        title = title,
        limit = 0,
        minimum = 0.0,
        cost = 0.0,
        weight = 0.0,
        notes = "",
        name = "",
        status = LegacyFreightInvoiceRuleStatus.NEW,
    )
}
