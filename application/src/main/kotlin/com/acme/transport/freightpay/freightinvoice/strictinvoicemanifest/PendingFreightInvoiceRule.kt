package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending freight invoice rule is in its life cycle. */
enum class PendingFreightInvoiceRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending freight invoice rule, with what the planners need to act on it. */
data class PendingFreightInvoiceRule(
    var code: String,
    var title: String,
    var quantity: Int,
    var ratio: Double,
    var weight: Double,
    var channel: String,
    var status: PendingFreightInvoiceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceRuleStatus): Boolean {
        when (status) {
            PendingFreightInvoiceRuleStatus.CREATED -> return next == PendingFreightInvoiceRuleStatus.RETIRED
            PendingFreightInvoiceRuleStatus.VALIDATED -> {
                when (next) {
                    PendingFreightInvoiceRuleStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingFreightInvoiceRuleStatus.PUBLISHED -> {
                when (next) {
                    PendingFreightInvoiceRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceRuleStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((quantity.toDouble() - weight) * 100) / 100

    fun snapshot(): PendingFreightInvoiceRule {
        return PendingFreightInvoiceRule(
            code = code,
            title = title,
            quantity = quantity,
            ratio = ratio,
            weight = weight,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending freight invoice rule in status created. */
fun newPendingFreightInvoiceRule(code: String, title: String): PendingFreightInvoiceRule {
    return PendingFreightInvoiceRule(
        code = code,
        title = title,
        quantity = 0,
        ratio = 0.0,
        weight = 0.0,
        channel = "",
        status = PendingFreightInvoiceRuleStatus.CREATED,
    )
}
