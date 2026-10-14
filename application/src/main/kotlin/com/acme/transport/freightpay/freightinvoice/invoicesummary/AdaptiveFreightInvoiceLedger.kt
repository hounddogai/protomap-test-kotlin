package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive freight invoice ledger is in its life cycle. */
enum class AdaptiveFreightInvoiceLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive freight invoice ledger, with what the planners need to act on it. */
data class AdaptiveFreightInvoiceLedger(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var minimum: Double,
    var margin: Double,
    var maximum: Double,
    var approved: Boolean,
    var odometer: Int,
    var contactEmail: String,
    var status: AdaptiveFreightInvoiceLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFreightInvoiceLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFreightInvoiceLedgerStatus): Boolean {
        when (status) {
            AdaptiveFreightInvoiceLedgerStatus.CREATED -> {
                when (next) {
                    AdaptiveFreightInvoiceLedgerStatus.VALIDATED, AdaptiveFreightInvoiceLedgerStatus.PUBLISHED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceLedgerStatus.VALIDATED -> return next == AdaptiveFreightInvoiceLedgerStatus.PUBLISHED || next == AdaptiveFreightInvoiceLedgerStatus.RETIRED
            AdaptiveFreightInvoiceLedgerStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveFreightInvoiceLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveFreightInvoiceLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive freight invoice ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(odometer.toDouble() * maximum * 100) / 100

    fun snapshot(): AdaptiveFreightInvoiceLedger {
        return AdaptiveFreightInvoiceLedger(
            code = code,
            title = title,
            tags = tags,
            minimum = minimum,
            margin = margin,
            maximum = maximum,
            approved = approved,
            odometer = odometer,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive freight invoice ledger in status created. */
fun newAdaptiveFreightInvoiceLedger(code: String, title: String): AdaptiveFreightInvoiceLedger {
    return AdaptiveFreightInvoiceLedger(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        margin = 0.0,
        maximum = 0.0,
        approved = false,
        odometer = 0,
        contactEmail = "",
        status = AdaptiveFreightInvoiceLedgerStatus.CREATED,
    )
}
