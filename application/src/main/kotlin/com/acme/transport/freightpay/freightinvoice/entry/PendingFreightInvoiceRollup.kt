package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending freight invoice rollup is in its life cycle. */
enum class PendingFreightInvoiceRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending freight invoice rollup and its current status. */
data class PendingFreightInvoiceRollup(
    var code: String,
    var label: String,
    var startsAt: Int,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var loadKg: Double,
    var limit: Int,
    var cost: Double,
    var status: PendingFreightInvoiceRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceRollupStatus): Boolean {
        when (status) {
            PendingFreightInvoiceRollupStatus.REQUESTED -> {
                when (next) {
                    PendingFreightInvoiceRollupStatus.EXPIRED -> return true
                }
                return false
            }
            PendingFreightInvoiceRollupStatus.GRANTED -> return next == PendingFreightInvoiceRollupStatus.REVOKED || next == PendingFreightInvoiceRollupStatus.EXPIRED
            PendingFreightInvoiceRollupStatus.REVOKED -> {
                when (next) {
                    PendingFreightInvoiceRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"pending freight invoice rollup"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((loadKg + startsAt.toDouble()) * 100) / 100

    fun snapshot(): PendingFreightInvoiceRollup {
        return PendingFreightInvoiceRollup(
            code = code,
            label = label,
            startsAt = startsAt,
            odometer = odometer,
            attributes = attributes,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            limit = limit,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending freight invoice rollup in status requested. */
fun newPendingFreightInvoiceRollup(code: String, label: String): PendingFreightInvoiceRollup {
    return PendingFreightInvoiceRollup(
        code = code,
        label = label,
        startsAt = 0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        loadKg = 0.0,
        limit = 0,
        cost = 0.0,
        status = PendingFreightInvoiceRollupStatus.REQUESTED,
    )
}
