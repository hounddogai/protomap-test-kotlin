package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hub variance is in its life cycle. */
enum class ScheduledHubVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled hub variance as the hub team keeps it. */
data class ScheduledHubVariance(
    var code: String,
    var label: String,
    var unit: String,
    var sequence: Int,
    var routeCode: String,
    var lane: String,
    var floorValue: Double,
    var distanceKm: Double,
    var status: ScheduledHubVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHubVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHubVarianceStatus): Boolean {
        when (status) {
            ScheduledHubVarianceStatus.PROPOSED -> {
                when (next) {
                    ScheduledHubVarianceStatus.APPROVED, ScheduledHubVarianceStatus.REJECTED -> return true
                }
                return false
            }
            ScheduledHubVarianceStatus.APPROVED -> {
                when (next) {
                    ScheduledHubVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledHubVarianceStatus.REJECTED -> {
                when (next) {
                    ScheduledHubVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHubVarianceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hub variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((distanceKm - sequence.toDouble()) * 100) / 100

    fun snapshot(): ScheduledHubVariance {
        return ScheduledHubVariance(
            code = code,
            label = label,
            unit = unit,
            sequence = sequence,
            routeCode = routeCode,
            lane = lane,
            floorValue = floorValue,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled hub variance in status proposed. */
fun newScheduledHubVariance(code: String, label: String): ScheduledHubVariance {
    return ScheduledHubVariance(
        code = code,
        label = label,
        unit = "",
        sequence = 0,
        routeCode = "",
        lane = "",
        floorValue = 0.0,
        distanceKm = 0.0,
        status = ScheduledHubVarianceStatus.PROPOSED,
    )
}
