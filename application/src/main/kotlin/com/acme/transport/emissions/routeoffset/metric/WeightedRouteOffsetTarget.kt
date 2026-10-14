package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted route offset target is in its life cycle. */
enum class WeightedRouteOffsetTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted route offset target as the route offset team keeps it. */
data class WeightedRouteOffsetTarget(
    var code: String,
    var title: String,
    var version: Int,
    var expiresAt: Int,
    var odometer: Int,
    var summary: String,
    var weight: Double,
    var unit: String,
    var score: Double,
    var lane: String,
    var status: WeightedRouteOffsetTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRouteOffsetTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRouteOffsetTargetStatus): Boolean {
        when (status) {
            WeightedRouteOffsetTargetStatus.PLANNED -> return next == WeightedRouteOffsetTargetStatus.IN_PROGRESS
            WeightedRouteOffsetTargetStatus.SCHEDULED -> return next == WeightedRouteOffsetTargetStatus.COMPLETED || next == WeightedRouteOffsetTargetStatus.CANCELLED
            WeightedRouteOffsetTargetStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedRouteOffsetTargetStatus.COMPLETED, WeightedRouteOffsetTargetStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedRouteOffsetTargetStatus.COMPLETED -> return next == WeightedRouteOffsetTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedRouteOffsetTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted route offset target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round(expiresAt.toDouble() * weight * 100) / 100

    fun snapshot(): WeightedRouteOffsetTarget {
        return WeightedRouteOffsetTarget(
            code = code,
            title = title,
            version = version,
            expiresAt = expiresAt,
            odometer = odometer,
            summary = summary,
            weight = weight,
            unit = unit,
            score = score,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted route offset target in status planned. */
fun newWeightedRouteOffsetTarget(code: String, title: String): WeightedRouteOffsetTarget {
    return WeightedRouteOffsetTarget(
        code = code,
        title = title,
        version = 0,
        expiresAt = 0,
        odometer = 0,
        summary = "",
        weight = 0.0,
        unit = "",
        score = 0.0,
        lane = "",
        status = WeightedRouteOffsetTargetStatus.PLANNED,
    )
}
