package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.text.normalize

// The capacity metric records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity metric is in its life cycle. */
enum class CapacityMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capacity metric as the capacity team keeps it. */
data class CapacityMetric(
    var code: String,
    var name: String,
    var lane: String,
    var active: Boolean,
    var startsAt: Int,
    var status: CapacityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityMetricStatus): Boolean {
        when (status) {
            CapacityMetricStatus.REQUESTED -> return next == CapacityMetricStatus.REVOKED || next == CapacityMetricStatus.EXPIRED
            CapacityMetricStatus.GRANTED -> return next == CapacityMetricStatus.REVOKED || next == CapacityMetricStatus.EXPIRED
            CapacityMetricStatus.REVOKED -> {
                when (next) {
                    CapacityMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityMetricStatus) {
        if (!canMoveTo(next)) {
            error("capacity metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): CapacityMetric {
        return CapacityMetric(
            code = code,
            name = name,
            lane = lane,
            active = active,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capacity metric in status requested. */
fun newCapacityMetric(code: String, name: String): CapacityMetric {
    return CapacityMetric(
        code = code,
        name = name,
        lane = "",
        active = false,
        startsAt = 0,
        status = CapacityMetricStatus.REQUESTED,
    )
}
