package com.acme.transport.network.node.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback node metric records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a fallback node metric is in its life cycle. */
enum class FallbackNodeMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback node metric. Its code is unique within a tenant. */
data class FallbackNodeMetric(
    var code: String,
    var title: String,
    var lane: String,
    var cost: Double,
    var active: Boolean,
    var segment: String,
    var maximum: Double,
    var status: FallbackNodeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNodeMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNodeMetricStatus): Boolean {
        when (status) {
            FallbackNodeMetricStatus.IDLE -> return next == FallbackNodeMetricStatus.READY || next == FallbackNodeMetricStatus.DRAINING
            FallbackNodeMetricStatus.WARMING -> return next == FallbackNodeMetricStatus.READY || next == FallbackNodeMetricStatus.DRAINING
            FallbackNodeMetricStatus.READY -> {
                when (next) {
                    FallbackNodeMetricStatus.DRAINING -> return true
                }
                return false
            }
            FallbackNodeMetricStatus.DRAINING -> return next == FallbackNodeMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackNodeMetricStatus) {
        if (!canMoveTo(next)) {
            error("fallback node metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback node metric"} $code"

    fun netVolume(): Double = round((cost - maximum) * 100) / 100

    fun snapshot(): FallbackNodeMetric {
        return FallbackNodeMetric(
            code = code,
            title = title,
            lane = lane,
            cost = cost,
            active = active,
            segment = segment,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback node metric in status idle. */
fun newFallbackNodeMetric(code: String, title: String): FallbackNodeMetric {
    return FallbackNodeMetric(
        code = code,
        title = title,
        lane = "",
        cost = 0.0,
        active = false,
        segment = "",
        maximum = 0.0,
        status = FallbackNodeMetricStatus.IDLE,
    )
}
