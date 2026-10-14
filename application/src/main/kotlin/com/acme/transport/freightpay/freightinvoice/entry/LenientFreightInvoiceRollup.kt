package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient freight invoice rollup is in its life cycle. */
enum class LenientFreightInvoiceRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient freight invoice rollup, with what the planners need to act on it. */
data class LenientFreightInvoiceRollup(
    var code: String,
    var label: String,
    var shiftCode: String,
    var graceDays: Int,
    var budget: Double,
    var status: LenientFreightInvoiceRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFreightInvoiceRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFreightInvoiceRollupStatus): Boolean {
        when (status) {
            LenientFreightInvoiceRollupStatus.OPEN -> {
                when (next) {
                    LenientFreightInvoiceRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientFreightInvoiceRollupStatus.IN_REVIEW -> {
                when (next) {
                    LenientFreightInvoiceRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientFreightInvoiceRollupStatus.RESOLVED -> {
                when (next) {
                    LenientFreightInvoiceRollupStatus.REOPENED -> return true
                }
                return false
            }
            LenientFreightInvoiceRollupStatus.REOPENED -> {
                when (next) {
                    LenientFreightInvoiceRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFreightInvoiceRollupStatus) {
        if (!canMoveTo(next)) {
            error("lenient freight invoice rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round(budget * graceDays.toDouble() * 100) / 100

    fun snapshot(): LenientFreightInvoiceRollup {
        return LenientFreightInvoiceRollup(
            code = code,
            label = label,
            shiftCode = shiftCode,
            graceDays = graceDays,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient freight invoice rollup in status open. */
fun newLenientFreightInvoiceRollup(code: String, label: String): LenientFreightInvoiceRollup {
    return LenientFreightInvoiceRollup(
        code = code,
        label = label,
        shiftCode = "",
        graceDays = 0,
        budget = 0.0,
        status = LenientFreightInvoiceRollupStatus.OPEN,
    )
}
