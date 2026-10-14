package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending service plan metric records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a pending service plan metric is in its life cycle. */
enum class PendingServicePlanMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a pending service plan metric and its current status. */
data class PendingServicePlanMetric(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var summary: String,
    var weight: Double,
    var floorValue: Double,
    var reviewer: String,
    var unit: String,
    var createdAt: Int,
    var status: PendingServicePlanMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServicePlanMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServicePlanMetricStatus): Boolean {
        when (status) {
            PendingServicePlanMetricStatus.IDLE -> return next == PendingServicePlanMetricStatus.DRAINING
            PendingServicePlanMetricStatus.WARMING -> return next == PendingServicePlanMetricStatus.READY || next == PendingServicePlanMetricStatus.STOPPED
            PendingServicePlanMetricStatus.READY -> {
                when (next) {
                    PendingServicePlanMetricStatus.DRAINING, PendingServicePlanMetricStatus.STOPPED -> return true
                }
                return false
            }
            PendingServicePlanMetricStatus.DRAINING -> return next == PendingServicePlanMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingServicePlanMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending service plan metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round((createdAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): PendingServicePlanMetric {
        return PendingServicePlanMetric(
            code = code,
            name = name,
            pinned = pinned,
            summary = summary,
            weight = weight,
            floorValue = floorValue,
            reviewer = reviewer,
            unit = unit,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending service plan metric in status idle. */
fun newPendingServicePlanMetric(code: String, name: String): PendingServicePlanMetric {
    return PendingServicePlanMetric(
        code = code,
        name = name,
        pinned = false,
        summary = "",
        weight = 0.0,
        floorValue = 0.0,
        reviewer = "",
        unit = "",
        createdAt = 0,
        status = PendingServicePlanMetricStatus.IDLE,
    )
}
