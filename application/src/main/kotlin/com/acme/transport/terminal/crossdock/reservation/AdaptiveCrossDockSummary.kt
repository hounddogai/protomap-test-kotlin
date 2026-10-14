package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive cross dock summary is in its life cycle. */
enum class AdaptiveCrossDockSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive cross dock summary. Its code is unique within a tenant. */
data class AdaptiveCrossDockSummary(
    var code: String,
    var name: String,
    var carrier: String,
    var revision: Int,
    var phone: String,
    var floorValue: Double,
    var windowDays: Int,
    var updatedAt: Int,
    var status: AdaptiveCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCrossDockSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCrossDockSummaryStatus): Boolean {
        when (status) {
            AdaptiveCrossDockSummaryStatus.CREATED -> return next == AdaptiveCrossDockSummaryStatus.VALIDATED
            AdaptiveCrossDockSummaryStatus.VALIDATED -> return next == AdaptiveCrossDockSummaryStatus.PUBLISHED || next == AdaptiveCrossDockSummaryStatus.RETIRED
            AdaptiveCrossDockSummaryStatus.PUBLISHED -> return next == AdaptiveCrossDockSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cross dock summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round(floorValue * windowDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveCrossDockSummary {
        return AdaptiveCrossDockSummary(
            code = code,
            name = name,
            carrier = carrier,
            revision = revision,
            phone = phone,
            floorValue = floorValue,
            windowDays = windowDays,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive cross dock summary in status created. */
fun newAdaptiveCrossDockSummary(code: String, name: String): AdaptiveCrossDockSummary {
    return AdaptiveCrossDockSummary(
        code = code,
        name = name,
        carrier = "",
        revision = 0,
        phone = "",
        floorValue = 0.0,
        windowDays = 0,
        updatedAt = 0,
        status = AdaptiveCrossDockSummaryStatus.CREATED,
    )
}
