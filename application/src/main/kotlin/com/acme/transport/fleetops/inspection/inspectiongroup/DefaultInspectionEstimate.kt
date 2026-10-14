package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default inspection estimate is in its life cycle. */
enum class DefaultInspectionEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One default inspection estimate, with what the planners need to act on it. */
data class DefaultInspectionEstimate(
    var code: String,
    var name: String,
    var startsAt: Int,
    var description: String,
    var loadKg: Double,
    var attributes: MutableMap<String, String>,
    var lane: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var locale: String,
    var status: DefaultInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionEstimateStatus): Boolean {
        when (status) {
            DefaultInspectionEstimateStatus.PLANNED -> {
                when (next) {
                    DefaultInspectionEstimateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefaultInspectionEstimateStatus.SCHEDULED -> {
                when (next) {
                    DefaultInspectionEstimateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefaultInspectionEstimateStatus.IN_PROGRESS -> return next == DefaultInspectionEstimateStatus.COMPLETED || next == DefaultInspectionEstimateStatus.CANCELLED
            DefaultInspectionEstimateStatus.COMPLETED -> return next == DefaultInspectionEstimateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("default inspection estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((fuelLiters + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultInspectionEstimate {
        return DefaultInspectionEstimate(
            code = code,
            name = name,
            startsAt = startsAt,
            description = description,
            loadKg = loadKg,
            attributes = attributes,
            lane = lane,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default inspection estimate in status planned. */
fun newDefaultInspectionEstimate(code: String, name: String): DefaultInspectionEstimate {
    return DefaultInspectionEstimate(
        code = code,
        name = name,
        startsAt = 0,
        description = "",
        loadKg = 0.0,
        attributes = mutableMapOf<String, String>(),
        lane = "",
        fuelLiters = 0.0,
        windowDays = 0,
        locale = "",
        status = DefaultInspectionEstimateStatus.PLANNED,
    )
}
