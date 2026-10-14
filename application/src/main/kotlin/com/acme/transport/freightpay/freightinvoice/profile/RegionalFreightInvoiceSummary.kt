package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional freight invoice summary is in its life cycle. */
enum class RegionalFreightInvoiceSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional freight invoice summary as the freight invoice team keeps it. */
data class RegionalFreightInvoiceSummary(
    var code: String,
    var title: String,
    var shiftCode: String,
    var windowDays: Int,
    var distanceKm: Double,
    var threshold: Double,
    var version: Int,
    var source: String,
    var durationMinutes: Int,
    var status: RegionalFreightInvoiceSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFreightInvoiceSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFreightInvoiceSummaryStatus): Boolean {
        when (status) {
            RegionalFreightInvoiceSummaryStatus.DRAFT -> {
                when (next) {
                    RegionalFreightInvoiceSummaryStatus.ACTIVE -> return true
                }
                return false
            }
            RegionalFreightInvoiceSummaryStatus.PENDING -> return next == RegionalFreightInvoiceSummaryStatus.CLOSED
            RegionalFreightInvoiceSummaryStatus.ACTIVE -> return next == RegionalFreightInvoiceSummaryStatus.SUSPENDED || next == RegionalFreightInvoiceSummaryStatus.CLOSED
            RegionalFreightInvoiceSummaryStatus.SUSPENDED -> return next == RegionalFreightInvoiceSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalFreightInvoiceSummaryStatus) {
        if (!canMoveTo(next)) {
            error("regional freight invoice summary $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((windowDays.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): RegionalFreightInvoiceSummary {
        return RegionalFreightInvoiceSummary(
            code = code,
            title = title,
            shiftCode = shiftCode,
            windowDays = windowDays,
            distanceKm = distanceKm,
            threshold = threshold,
            version = version,
            source = source,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional freight invoice summary in status draft. */
fun newRegionalFreightInvoiceSummary(code: String, title: String): RegionalFreightInvoiceSummary {
    return RegionalFreightInvoiceSummary(
        code = code,
        title = title,
        shiftCode = "",
        windowDays = 0,
        distanceKm = 0.0,
        threshold = 0.0,
        version = 0,
        source = "",
        durationMinutes = 0,
        status = RegionalFreightInvoiceSummaryStatus.DRAFT,
    )
}
