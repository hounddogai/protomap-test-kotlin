package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive freight invoice summary is in its life cycle. */
enum class AdaptiveFreightInvoiceSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive freight invoice summary as the freight invoice team keeps it. */
data class AdaptiveFreightInvoiceSummary(
    var code: String,
    var name: String,
    var capacity: Int,
    var floorValue: Double,
    var batchSize: Int,
    var startsAt: Int,
    var durationMinutes: Int,
    var source: String,
    var retries: Int,
    var lane: String,
    var updatedAt: Int,
    var status: AdaptiveFreightInvoiceSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFreightInvoiceSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFreightInvoiceSummaryStatus): Boolean {
        when (status) {
            AdaptiveFreightInvoiceSummaryStatus.IDLE -> return next == AdaptiveFreightInvoiceSummaryStatus.READY || next == AdaptiveFreightInvoiceSummaryStatus.STOPPED
            AdaptiveFreightInvoiceSummaryStatus.WARMING -> {
                when (next) {
                    AdaptiveFreightInvoiceSummaryStatus.DRAINING, AdaptiveFreightInvoiceSummaryStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceSummaryStatus.READY -> {
                when (next) {
                    AdaptiveFreightInvoiceSummaryStatus.DRAINING, AdaptiveFreightInvoiceSummaryStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceSummaryStatus.DRAINING -> return next == AdaptiveFreightInvoiceSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveFreightInvoiceSummaryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive freight invoice summary $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round((floorValue + capacity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveFreightInvoiceSummary {
        return AdaptiveFreightInvoiceSummary(
            code = code,
            name = name,
            capacity = capacity,
            floorValue = floorValue,
            batchSize = batchSize,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            source = source,
            retries = retries,
            lane = lane,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive freight invoice summary in status idle. */
fun newAdaptiveFreightInvoiceSummary(code: String, name: String): AdaptiveFreightInvoiceSummary {
    return AdaptiveFreightInvoiceSummary(
        code = code,
        name = name,
        capacity = 0,
        floorValue = 0.0,
        batchSize = 0,
        startsAt = 0,
        durationMinutes = 0,
        source = "",
        retries = 0,
        lane = "",
        updatedAt = 0,
        status = AdaptiveFreightInvoiceSummaryStatus.IDLE,
    )
}
