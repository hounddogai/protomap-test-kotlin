package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.normalize

// The near miss metric records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss metric is in its life cycle. */
enum class NearMissMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a near miss metric and its current status. */
data class NearMissMetric(
    var code: String,
    var label: String,
    var trailerId: String,
    var lane: String,
    var fuelLiters: Double,
    var status: NearMissMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissMetricStatus): Boolean {
        when (status) {
            NearMissMetricStatus.IDLE -> return next == NearMissMetricStatus.WARMING || next == NearMissMetricStatus.STOPPED
            NearMissMetricStatus.WARMING -> {
                when (next) {
                    NearMissMetricStatus.DRAINING -> return true
                }
                return false
            }
            NearMissMetricStatus.READY -> return next == NearMissMetricStatus.STOPPED
            NearMissMetricStatus.DRAINING -> return next == NearMissMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: NearMissMetricStatus) {
        if (!canMoveTo(next)) {
            error("near miss metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): NearMissMetric {
        return NearMissMetric(
            code = code,
            label = label,
            trailerId = trailerId,
            lane = lane,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss metric in status idle. */
fun newNearMissMetric(code: String, label: String): NearMissMetric {
    return NearMissMetric(
        code = code,
        label = label,
        trailerId = "",
        lane = "",
        fuelLiters = 0.0,
        status = NearMissMetricStatus.IDLE,
    )
}
