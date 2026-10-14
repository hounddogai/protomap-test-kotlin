package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling route offset metric records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a rolling route offset metric is in its life cycle. */
enum class RollingRouteOffsetMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling route offset metric and its current status. */
data class RollingRouteOffsetMetric(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var pallets: Int,
    var fuelLiters: Double,
    var unit: String,
    var threshold: Double,
    var capacity: Int,
    var trailerId: String,
    var status: RollingRouteOffsetMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRouteOffsetMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRouteOffsetMetricStatus): Boolean {
        when (status) {
            RollingRouteOffsetMetricStatus.OPEN -> return next == RollingRouteOffsetMetricStatus.ARCHIVED
            RollingRouteOffsetMetricStatus.IN_REVIEW -> {
                when (next) {
                    RollingRouteOffsetMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingRouteOffsetMetricStatus.RESOLVED -> return next == RollingRouteOffsetMetricStatus.ARCHIVED
            RollingRouteOffsetMetricStatus.REOPENED -> return next == RollingRouteOffsetMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingRouteOffsetMetricStatus) {
        if (!canMoveTo(next)) {
            error("rolling route offset metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((capacity.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): RollingRouteOffsetMetric {
        return RollingRouteOffsetMetric(
            code = code,
            title = title,
            attributes = attributes,
            ceiling = ceiling,
            pallets = pallets,
            fuelLiters = fuelLiters,
            unit = unit,
            threshold = threshold,
            capacity = capacity,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling route offset metric in status open. */
fun newRollingRouteOffsetMetric(code: String, title: String): RollingRouteOffsetMetric {
    return RollingRouteOffsetMetric(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        pallets = 0,
        fuelLiters = 0.0,
        unit = "",
        threshold = 0.0,
        capacity = 0,
        trailerId = "",
        status = RollingRouteOffsetMetricStatus.OPEN,
    )
}
