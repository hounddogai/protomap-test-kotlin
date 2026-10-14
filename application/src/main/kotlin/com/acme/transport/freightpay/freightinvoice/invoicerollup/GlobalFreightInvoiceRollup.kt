package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global freight invoice rollup records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a global freight invoice rollup is in its life cycle. */
enum class GlobalFreightInvoiceRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a global freight invoice rollup and its current status. */
data class GlobalFreightInvoiceRollup(
    var code: String,
    var name: String,
    var trailerId: String,
    var title: String,
    var odometer: Int,
    var weight: Double,
    var ceiling: Double,
    var checksum: String,
    var score: Double,
    var threshold: Double,
    var status: GlobalFreightInvoiceRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFreightInvoiceRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFreightInvoiceRollupStatus): Boolean {
        when (status) {
            GlobalFreightInvoiceRollupStatus.IDLE -> {
                when (next) {
                    GlobalFreightInvoiceRollupStatus.READY -> return true
                }
                return false
            }
            GlobalFreightInvoiceRollupStatus.WARMING -> {
                when (next) {
                    GlobalFreightInvoiceRollupStatus.READY, GlobalFreightInvoiceRollupStatus.STOPPED -> return true
                }
                return false
            }
            GlobalFreightInvoiceRollupStatus.READY -> return next == GlobalFreightInvoiceRollupStatus.DRAINING
            GlobalFreightInvoiceRollupStatus.DRAINING -> {
                when (next) {
                    GlobalFreightInvoiceRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFreightInvoiceRollupStatus) {
        if (!canMoveTo(next)) {
            error("global freight invoice rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round((score + ceiling) * 100) / 100

    fun snapshot(): GlobalFreightInvoiceRollup {
        return GlobalFreightInvoiceRollup(
            code = code,
            name = name,
            trailerId = trailerId,
            title = title,
            odometer = odometer,
            weight = weight,
            ceiling = ceiling,
            checksum = checksum,
            score = score,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global freight invoice rollup in status idle. */
fun newGlobalFreightInvoiceRollup(code: String, name: String): GlobalFreightInvoiceRollup {
    return GlobalFreightInvoiceRollup(
        code = code,
        name = name,
        trailerId = "",
        title = "",
        odometer = 0,
        weight = 0.0,
        ceiling = 0.0,
        checksum = "",
        score = 0.0,
        threshold = 0.0,
        status = GlobalFreightInvoiceRollupStatus.IDLE,
    )
}
