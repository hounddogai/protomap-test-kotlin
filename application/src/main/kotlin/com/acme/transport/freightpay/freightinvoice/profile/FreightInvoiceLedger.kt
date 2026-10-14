package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice ledger is in its life cycle. */
enum class FreightInvoiceLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One freight invoice ledger, with what the planners need to act on it. */
data class FreightInvoiceLedger(
    var code: String,
    var name: String,
    var attempts: Int,
    var depot: String,
    var updatedAt: Int,
    var maximum: Double,
    var ownerId: String,
    var discount: Double,
    var phone: String,
    var lane: String,
    var status: FreightInvoiceLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceLedgerStatus): Boolean {
        when (status) {
            FreightInvoiceLedgerStatus.OPEN -> return next == FreightInvoiceLedgerStatus.REOPENED || next == FreightInvoiceLedgerStatus.ARCHIVED
            FreightInvoiceLedgerStatus.IN_REVIEW -> {
                when (next) {
                    FreightInvoiceLedgerStatus.REOPENED, FreightInvoiceLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            FreightInvoiceLedgerStatus.RESOLVED -> return next == FreightInvoiceLedgerStatus.ARCHIVED
            FreightInvoiceLedgerStatus.REOPENED -> {
                when (next) {
                    FreightInvoiceLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceLedgerStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice ledger $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((discount + maximum) * 100) / 100

    fun snapshot(): FreightInvoiceLedger {
        return FreightInvoiceLedger(
            code = code,
            name = name,
            attempts = attempts,
            depot = depot,
            updatedAt = updatedAt,
            maximum = maximum,
            ownerId = ownerId,
            discount = discount,
            phone = phone,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a freight invoice ledger in status open. */
fun newFreightInvoiceLedger(code: String, name: String): FreightInvoiceLedger {
    return FreightInvoiceLedger(
        code = code,
        name = name,
        attempts = 0,
        depot = "",
        updatedAt = 0,
        maximum = 0.0,
        ownerId = "",
        discount = 0.0,
        phone = "",
        lane = "",
        status = FreightInvoiceLedgerStatus.OPEN,
    )
}
