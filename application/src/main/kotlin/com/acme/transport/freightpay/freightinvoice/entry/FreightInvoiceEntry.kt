package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice entry records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice entry is in its life cycle. */
enum class FreightInvoiceEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A freight invoice entry. Its code is unique within a tenant. */
data class FreightInvoiceEntry(
    var code: String,
    var label: String,
    var odometer: Int,
    var channel: String,
    var createdAt: Int,
    var startsAt: Int,
    var status: FreightInvoiceEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceEntryStatus): Boolean {
        when (status) {
            FreightInvoiceEntryStatus.PROPOSED -> {
                when (next) {
                    FreightInvoiceEntryStatus.APPROVED, FreightInvoiceEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            FreightInvoiceEntryStatus.APPROVED -> {
                when (next) {
                    FreightInvoiceEntryStatus.REJECTED -> return true
                }
                return false
            }
            FreightInvoiceEntryStatus.REJECTED -> return next == FreightInvoiceEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FreightInvoiceEntryStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"freight invoice entry"} $code"

    fun adjustedVolume(): Double = round((createdAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceEntry {
        return FreightInvoiceEntry(
            code = code,
            label = label,
            odometer = odometer,
            channel = channel,
            createdAt = createdAt,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a freight invoice entry in status proposed. */
fun newFreightInvoiceEntry(code: String, label: String): FreightInvoiceEntry {
    return FreightInvoiceEntry(
        code = code,
        label = label,
        odometer = 0,
        channel = "",
        createdAt = 0,
        startsAt = 0,
        status = FreightInvoiceEntryStatus.PROPOSED,
    )
}
