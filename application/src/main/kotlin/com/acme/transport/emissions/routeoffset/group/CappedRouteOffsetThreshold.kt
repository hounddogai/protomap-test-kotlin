package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped route offset threshold is in its life cycle. */
enum class CappedRouteOffsetThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped route offset threshold as the route offset team keeps it. */
data class CappedRouteOffsetThreshold(
    var code: String,
    var name: String,
    var region: String,
    var budget: Double,
    var maximum: Double,
    var status: CappedRouteOffsetThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRouteOffsetThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRouteOffsetThresholdStatus): Boolean {
        when (status) {
            CappedRouteOffsetThresholdStatus.PLANNED -> {
                when (next) {
                    CappedRouteOffsetThresholdStatus.IN_PROGRESS, CappedRouteOffsetThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            CappedRouteOffsetThresholdStatus.SCHEDULED -> return next == CappedRouteOffsetThresholdStatus.IN_PROGRESS
            CappedRouteOffsetThresholdStatus.IN_PROGRESS -> {
                when (next) {
                    CappedRouteOffsetThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            CappedRouteOffsetThresholdStatus.COMPLETED -> {
                when (next) {
                    CappedRouteOffsetThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRouteOffsetThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped route offset threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped route offset threshold"} $code"

    fun effectiveExposure(): Double = round((budget + maximum) * 100) / 100

    fun snapshot(): CappedRouteOffsetThreshold {
        return CappedRouteOffsetThreshold(
            code = code,
            name = name,
            region = region,
            budget = budget,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped route offset threshold in status planned. */
fun newCappedRouteOffsetThreshold(code: String, name: String): CappedRouteOffsetThreshold {
    return CappedRouteOffsetThreshold(
        code = code,
        name = name,
        region = "",
        budget = 0.0,
        maximum = 0.0,
        status = CappedRouteOffsetThresholdStatus.PLANNED,
    )
}
