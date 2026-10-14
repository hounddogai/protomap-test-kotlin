package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling camera event metric is in its life cycle. */
enum class RollingCameraEventMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling camera event metric, with what the planners need to act on it. */
data class RollingCameraEventMetric(
    var code: String,
    var name: String,
    var driverId: String,
    var reviewer: String,
    var title: String,
    var lane: String,
    var attempts: Int,
    var trailerId: String,
    var quantity: Int,
    var status: RollingCameraEventMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCameraEventMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCameraEventMetricStatus): Boolean {
        when (status) {
            RollingCameraEventMetricStatus.REQUESTED -> {
                when (next) {
                    RollingCameraEventMetricStatus.REVOKED -> return true
                }
                return false
            }
            RollingCameraEventMetricStatus.GRANTED -> return next == RollingCameraEventMetricStatus.REVOKED || next == RollingCameraEventMetricStatus.EXPIRED
            RollingCameraEventMetricStatus.REVOKED -> return next == RollingCameraEventMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingCameraEventMetricStatus) {
        if (!canMoveTo(next)) {
            error("rolling camera event metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((attempts.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RollingCameraEventMetric {
        return RollingCameraEventMetric(
            code = code,
            name = name,
            driverId = driverId,
            reviewer = reviewer,
            title = title,
            lane = lane,
            attempts = attempts,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling camera event metric in status requested. */
fun newRollingCameraEventMetric(code: String, name: String): RollingCameraEventMetric {
    return RollingCameraEventMetric(
        code = code,
        name = name,
        driverId = "",
        reviewer = "",
        title = "",
        lane = "",
        attempts = 0,
        trailerId = "",
        quantity = 0,
        status = RollingCameraEventMetricStatus.REQUESTED,
    )
}
