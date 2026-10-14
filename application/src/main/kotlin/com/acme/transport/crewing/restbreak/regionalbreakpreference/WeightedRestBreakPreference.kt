package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted rest break preference records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a weighted rest break preference is in its life cycle. */
enum class WeightedRestBreakPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted rest break preference as the rest break team keeps it. */
data class WeightedRestBreakPreference(
    var code: String,
    var name: String,
    var maximum: Double,
    var distanceKm: Double,
    var etaMinutes: Int,
    var category: String,
    var fuelLiters: Double,
    var routeCode: String,
    var status: WeightedRestBreakPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRestBreakPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRestBreakPreferenceStatus): Boolean {
        when (status) {
            WeightedRestBreakPreferenceStatus.PLANNED -> {
                when (next) {
                    WeightedRestBreakPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedRestBreakPreferenceStatus.SCHEDULED -> return next == WeightedRestBreakPreferenceStatus.IN_PROGRESS
            WeightedRestBreakPreferenceStatus.IN_PROGRESS -> return next == WeightedRestBreakPreferenceStatus.COMPLETED || next == WeightedRestBreakPreferenceStatus.CANCELLED
            WeightedRestBreakPreferenceStatus.COMPLETED -> {
                when (next) {
                    WeightedRestBreakPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRestBreakPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("weighted rest break preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((fuelLiters + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedRestBreakPreference {
        return WeightedRestBreakPreference(
            code = code,
            name = name,
            maximum = maximum,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            category = category,
            fuelLiters = fuelLiters,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted rest break preference in status planned. */
fun newWeightedRestBreakPreference(code: String, name: String): WeightedRestBreakPreference {
    return WeightedRestBreakPreference(
        code = code,
        name = name,
        maximum = 0.0,
        distanceKm = 0.0,
        etaMinutes = 0,
        category = "",
        fuelLiters = 0.0,
        routeCode = "",
        status = WeightedRestBreakPreferenceStatus.PLANNED,
    )
}
