package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy service plan metric is in its life cycle. */
enum class LegacyServicePlanMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One legacy service plan metric, with what the planners need to act on it. */
data class LegacyServicePlanMetric(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var country: String,
    var weight: Double,
    var updatedAt: Int,
    var ratio: Double,
    var title: String,
    var carrier: String,
    var trailerId: String,
    var status: LegacyServicePlanMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServicePlanMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServicePlanMetricStatus): Boolean {
        when (status) {
            LegacyServicePlanMetricStatus.PLANNED -> {
                when (next) {
                    LegacyServicePlanMetricStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyServicePlanMetricStatus.SCHEDULED -> return next == LegacyServicePlanMetricStatus.IN_PROGRESS
            LegacyServicePlanMetricStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyServicePlanMetricStatus.COMPLETED, LegacyServicePlanMetricStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyServicePlanMetricStatus.COMPLETED -> {
                when (next) {
                    LegacyServicePlanMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyServicePlanMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy service plan metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round(fuelLiters * updatedAt.toDouble() * 100) / 100

    fun snapshot(): LegacyServicePlanMetric {
        return LegacyServicePlanMetric(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            tags = tags,
            country = country,
            weight = weight,
            updatedAt = updatedAt,
            ratio = ratio,
            title = title,
            carrier = carrier,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy service plan metric in status planned. */
fun newLegacyServicePlanMetric(code: String, label: String): LegacyServicePlanMetric {
    return LegacyServicePlanMetric(
        code = code,
        label = label,
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        country = "",
        weight = 0.0,
        updatedAt = 0,
        ratio = 0.0,
        title = "",
        carrier = "",
        trailerId = "",
        status = LegacyServicePlanMetricStatus.PLANNED,
    )
}
