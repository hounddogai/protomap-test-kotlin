package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global route offset summary is in its life cycle. */
enum class GlobalRouteOffsetSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global route offset summary. Its code is unique within a tenant. */
data class GlobalRouteOffsetSummary(
    var code: String,
    var label: String,
    var startsAt: Int,
    var ceiling: Double,
    var quantity: Int,
    var endsAt: Int,
    var status: GlobalRouteOffsetSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRouteOffsetSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRouteOffsetSummaryStatus): Boolean {
        when (status) {
            GlobalRouteOffsetSummaryStatus.REQUESTED -> return next == GlobalRouteOffsetSummaryStatus.GRANTED || next == GlobalRouteOffsetSummaryStatus.EXPIRED
            GlobalRouteOffsetSummaryStatus.GRANTED -> return next == GlobalRouteOffsetSummaryStatus.EXPIRED
            GlobalRouteOffsetSummaryStatus.REVOKED -> {
                when (next) {
                    GlobalRouteOffsetSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRouteOffsetSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global route offset summary $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round(quantity.toDouble() * ceiling * 100) / 100

    fun snapshot(): GlobalRouteOffsetSummary {
        return GlobalRouteOffsetSummary(
            code = code,
            label = label,
            startsAt = startsAt,
            ceiling = ceiling,
            quantity = quantity,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global route offset summary in status requested. */
fun newGlobalRouteOffsetSummary(code: String, label: String): GlobalRouteOffsetSummary {
    return GlobalRouteOffsetSummary(
        code = code,
        label = label,
        startsAt = 0,
        ceiling = 0.0,
        quantity = 0,
        endsAt = 0,
        status = GlobalRouteOffsetSummaryStatus.REQUESTED,
    )
}
