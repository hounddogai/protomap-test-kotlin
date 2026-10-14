package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered freight invoice variance is in its life cycle. */
enum class TieredFreightInvoiceVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered freight invoice variance as the freight invoice team keeps it. */
data class TieredFreightInvoiceVariance(
    var code: String,
    var title: String,
    var capacity: Int,
    var graceDays: Int,
    var summary: String,
    var durationMinutes: Int,
    var rate: Double,
    var status: TieredFreightInvoiceVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFreightInvoiceVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFreightInvoiceVarianceStatus): Boolean {
        when (status) {
            TieredFreightInvoiceVarianceStatus.PROPOSED -> {
                when (next) {
                    TieredFreightInvoiceVarianceStatus.REJECTED, TieredFreightInvoiceVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredFreightInvoiceVarianceStatus.APPROVED -> return next == TieredFreightInvoiceVarianceStatus.REJECTED
            TieredFreightInvoiceVarianceStatus.REJECTED -> {
                when (next) {
                    TieredFreightInvoiceVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFreightInvoiceVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered freight invoice variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered freight invoice variance"} $code"

    fun effectiveExposure(): Double = round((graceDays.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredFreightInvoiceVariance {
        return TieredFreightInvoiceVariance(
            code = code,
            title = title,
            capacity = capacity,
            graceDays = graceDays,
            summary = summary,
            durationMinutes = durationMinutes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered freight invoice variance in status proposed. */
fun newTieredFreightInvoiceVariance(code: String, title: String): TieredFreightInvoiceVariance {
    return TieredFreightInvoiceVariance(
        code = code,
        title = title,
        capacity = 0,
        graceDays = 0,
        summary = "",
        durationMinutes = 0,
        rate = 0.0,
        status = TieredFreightInvoiceVarianceStatus.PROPOSED,
    )
}
