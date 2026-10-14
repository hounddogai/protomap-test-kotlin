package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice tier is in its life cycle. */
enum class FreightInvoiceTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One freight invoice tier, with what the planners need to act on it. */
data class FreightInvoiceTier(
    var code: String,
    var name: String,
    var visible: Boolean,
    var notes: String,
    var reviewer: String,
    var pallets: Int,
    var currency: String,
    var cost: Double,
    var description: String,
    var status: FreightInvoiceTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceTierStatus): Boolean {
        when (status) {
            FreightInvoiceTierStatus.OPEN -> return next == FreightInvoiceTierStatus.IN_REVIEW || next == FreightInvoiceTierStatus.ARCHIVED
            FreightInvoiceTierStatus.IN_REVIEW -> {
                when (next) {
                    FreightInvoiceTierStatus.REOPENED -> return true
                }
                return false
            }
            FreightInvoiceTierStatus.RESOLVED -> return next == FreightInvoiceTierStatus.ARCHIVED
            FreightInvoiceTierStatus.REOPENED -> {
                when (next) {
                    FreightInvoiceTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceTierStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round(pallets.toDouble() * cost * 100) / 100

    fun snapshot(): FreightInvoiceTier {
        return FreightInvoiceTier(
            code = code,
            name = name,
            visible = visible,
            notes = notes,
            reviewer = reviewer,
            pallets = pallets,
            currency = currency,
            cost = cost,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a freight invoice tier in status open. */
fun newFreightInvoiceTier(code: String, name: String): FreightInvoiceTier {
    return FreightInvoiceTier(
        code = code,
        name = name,
        visible = false,
        notes = "",
        reviewer = "",
        pallets = 0,
        currency = "",
        cost = 0.0,
        description = "",
        status = FreightInvoiceTierStatus.OPEN,
    )
}
