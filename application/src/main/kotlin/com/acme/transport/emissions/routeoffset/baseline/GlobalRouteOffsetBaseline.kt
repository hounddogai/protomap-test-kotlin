package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global route offset baseline is in its life cycle. */
enum class GlobalRouteOffsetBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global route offset baseline and its current status. */
data class GlobalRouteOffsetBaseline(
    var code: String,
    var name: String,
    var threshold: Double,
    var carrier: String,
    var rate: Double,
    var description: String,
    var status: GlobalRouteOffsetBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRouteOffsetBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRouteOffsetBaselineStatus): Boolean {
        when (status) {
            GlobalRouteOffsetBaselineStatus.PLANNED -> return next == GlobalRouteOffsetBaselineStatus.COMPLETED
            GlobalRouteOffsetBaselineStatus.SCHEDULED -> return next == GlobalRouteOffsetBaselineStatus.COMPLETED
            GlobalRouteOffsetBaselineStatus.IN_PROGRESS -> return next == GlobalRouteOffsetBaselineStatus.CANCELLED
            GlobalRouteOffsetBaselineStatus.COMPLETED -> {
                when (next) {
                    GlobalRouteOffsetBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRouteOffsetBaselineStatus) {
        if (!canMoveTo(next)) {
            error("global route offset baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((rate + threshold) * 100) / 100

    fun snapshot(): GlobalRouteOffsetBaseline {
        return GlobalRouteOffsetBaseline(
            code = code,
            name = name,
            threshold = threshold,
            carrier = carrier,
            rate = rate,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global route offset baseline in status planned. */
fun newGlobalRouteOffsetBaseline(code: String, name: String): GlobalRouteOffsetBaseline {
    return GlobalRouteOffsetBaseline(
        code = code,
        name = name,
        threshold = 0.0,
        carrier = "",
        rate = 0.0,
        description = "",
        status = GlobalRouteOffsetBaselineStatus.PLANNED,
    )
}
