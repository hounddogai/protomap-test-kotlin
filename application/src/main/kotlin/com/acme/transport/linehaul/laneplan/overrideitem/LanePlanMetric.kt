package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan metric is in its life cycle. */
enum class LanePlanMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lane plan metric. Its code is unique within a tenant. */
data class LanePlanMetric(
    var code: String,
    var name: String,
    var startsAt: Int,
    var pinned: Boolean,
    var lane: String,
    var timezone: String,
    var quantity: Int,
    var limit: Int,
    var budget: Double,
    var version: Int,
    var vehicleId: String,
    var status: LanePlanMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanMetricStatus): Boolean {
        when (status) {
            LanePlanMetricStatus.PROPOSED -> {
                when (next) {
                    LanePlanMetricStatus.APPROVED, LanePlanMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            LanePlanMetricStatus.APPROVED -> {
                when (next) {
                    LanePlanMetricStatus.REJECTED, LanePlanMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            LanePlanMetricStatus.REJECTED -> {
                when (next) {
                    LanePlanMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanMetricStatus) {
        if (!canMoveTo(next)) {
            error("lane plan metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round((limit.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LanePlanMetric {
        return LanePlanMetric(
            code = code,
            name = name,
            startsAt = startsAt,
            pinned = pinned,
            lane = lane,
            timezone = timezone,
            quantity = quantity,
            limit = limit,
            budget = budget,
            version = version,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lane plan metric in status proposed. */
fun newLanePlanMetric(code: String, name: String): LanePlanMetric {
    return LanePlanMetric(
        code = code,
        name = name,
        startsAt = 0,
        pinned = false,
        lane = "",
        timezone = "",
        quantity = 0,
        limit = 0,
        budget = 0.0,
        version = 0,
        vehicleId = "",
        status = LanePlanMetricStatus.PROPOSED,
    )
}
