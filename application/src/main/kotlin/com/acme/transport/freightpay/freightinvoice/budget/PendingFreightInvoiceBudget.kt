package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending freight invoice budget records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a pending freight invoice budget is in its life cycle. */
enum class PendingFreightInvoiceBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending freight invoice budget, with what the planners need to act on it. */
data class PendingFreightInvoiceBudget(
    var code: String,
    var title: String,
    var maximum: Double,
    var threshold: Double,
    var country: String,
    var margin: Double,
    var status: PendingFreightInvoiceBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceBudgetStatus): Boolean {
        when (status) {
            PendingFreightInvoiceBudgetStatus.NEW -> return next == PendingFreightInvoiceBudgetStatus.RUNNING || next == PendingFreightInvoiceBudgetStatus.DONE
            PendingFreightInvoiceBudgetStatus.QUEUED -> return next == PendingFreightInvoiceBudgetStatus.DONE
            PendingFreightInvoiceBudgetStatus.RUNNING -> {
                when (next) {
                    PendingFreightInvoiceBudgetStatus.DONE, PendingFreightInvoiceBudgetStatus.FAILED -> return true
                }
                return false
            }
            PendingFreightInvoiceBudgetStatus.DONE -> return next == PendingFreightInvoiceBudgetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending freight invoice budget"} $code"

    fun netValue(): Double = round((threshold - maximum) * 100) / 100

    fun snapshot(): PendingFreightInvoiceBudget {
        return PendingFreightInvoiceBudget(
            code = code,
            title = title,
            maximum = maximum,
            threshold = threshold,
            country = country,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending freight invoice budget in status new. */
fun newPendingFreightInvoiceBudget(code: String, title: String): PendingFreightInvoiceBudget {
    return PendingFreightInvoiceBudget(
        code = code,
        title = title,
        maximum = 0.0,
        threshold = 0.0,
        country = "",
        margin = 0.0,
        status = PendingFreightInvoiceBudgetStatus.NEW,
    )
}
