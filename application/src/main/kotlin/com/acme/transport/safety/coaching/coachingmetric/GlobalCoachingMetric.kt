package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global coaching metric records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a global coaching metric is in its life cycle. */
enum class GlobalCoachingMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global coaching metric as the coaching team keeps it. */
data class GlobalCoachingMetric(
    var code: String,
    var name: String,
    var quantity: Int,
    var pallets: Int,
    var depot: String,
    var pinned: Boolean,
    var floorValue: Double,
    var status: GlobalCoachingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingMetricStatus): Boolean {
        when (status) {
            GlobalCoachingMetricStatus.PLANNED -> return next == GlobalCoachingMetricStatus.SCHEDULED
            GlobalCoachingMetricStatus.SCHEDULED -> return next == GlobalCoachingMetricStatus.COMPLETED || next == GlobalCoachingMetricStatus.CANCELLED
            GlobalCoachingMetricStatus.IN_PROGRESS -> return next == GlobalCoachingMetricStatus.COMPLETED || next == GlobalCoachingMetricStatus.CANCELLED
            GlobalCoachingMetricStatus.COMPLETED -> {
                when (next) {
                    GlobalCoachingMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCoachingMetricStatus) {
        if (!canMoveTo(next)) {
            error("global coaching metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global coaching metric"} $code"

    fun netExposure(): Double = round((pallets.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): GlobalCoachingMetric {
        return GlobalCoachingMetric(
            code = code,
            name = name,
            quantity = quantity,
            pallets = pallets,
            depot = depot,
            pinned = pinned,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global coaching metric in status planned. */
fun newGlobalCoachingMetric(code: String, name: String): GlobalCoachingMetric {
    return GlobalCoachingMetric(
        code = code,
        name = name,
        quantity = 0,
        pallets = 0,
        depot = "",
        pinned = false,
        floorValue = 0.0,
        status = GlobalCoachingMetricStatus.PLANNED,
    )
}
