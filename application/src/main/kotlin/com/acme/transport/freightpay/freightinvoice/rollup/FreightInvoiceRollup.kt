package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice rollup records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice rollup is in its life cycle. */
enum class FreightInvoiceRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a freight invoice rollup and its current status. */
data class FreightInvoiceRollup(
    var code: String,
    var title: String,
    var ratio: Double,
    var score: Double,
    var source: String,
    var status: FreightInvoiceRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceRollupStatus): Boolean {
        when (status) {
            FreightInvoiceRollupStatus.PROPOSED -> return next == FreightInvoiceRollupStatus.APPROVED || next == FreightInvoiceRollupStatus.WITHDRAWN
            FreightInvoiceRollupStatus.APPROVED -> return next == FreightInvoiceRollupStatus.WITHDRAWN
            FreightInvoiceRollupStatus.REJECTED -> {
                when (next) {
                    FreightInvoiceRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceRollupStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round(score * ratio * 100) / 100

    fun snapshot(): FreightInvoiceRollup {
        return FreightInvoiceRollup(
            code = code,
            title = title,
            ratio = ratio,
            score = score,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a freight invoice rollup in status proposed. */
fun newFreightInvoiceRollup(code: String, title: String): FreightInvoiceRollup {
    return FreightInvoiceRollup(
        code = code,
        title = title,
        ratio = 0.0,
        score = 0.0,
        source = "",
        status = FreightInvoiceRollupStatus.PROPOSED,
    )
}
