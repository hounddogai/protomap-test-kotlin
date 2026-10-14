package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted roster tier is in its life cycle. */
enum class WeightedRosterTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted roster tier. Its code is unique within a tenant. */
data class WeightedRosterTier(
    var code: String,
    var title: String,
    var revision: Int,
    var maximum: Double,
    var notes: String,
    var windowDays: Int,
    var rate: Double,
    var odometer: Int,
    var shiftCode: String,
    var fuelLiters: Double,
    var status: WeightedRosterTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRosterTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRosterTierStatus): Boolean {
        when (status) {
            WeightedRosterTierStatus.PROPOSED -> return next == WeightedRosterTierStatus.APPROVED || next == WeightedRosterTierStatus.REJECTED
            WeightedRosterTierStatus.APPROVED -> return next == WeightedRosterTierStatus.WITHDRAWN
            WeightedRosterTierStatus.REJECTED -> {
                when (next) {
                    WeightedRosterTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRosterTierStatus) {
        if (!canMoveTo(next)) {
            error("weighted roster tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((maximum - rate) * 100) / 100

    fun snapshot(): WeightedRosterTier {
        return WeightedRosterTier(
            code = code,
            title = title,
            revision = revision,
            maximum = maximum,
            notes = notes,
            windowDays = windowDays,
            rate = rate,
            odometer = odometer,
            shiftCode = shiftCode,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted roster tier in status proposed. */
fun newWeightedRosterTier(code: String, title: String): WeightedRosterTier {
    return WeightedRosterTier(
        code = code,
        title = title,
        revision = 0,
        maximum = 0.0,
        notes = "",
        windowDays = 0,
        rate = 0.0,
        odometer = 0,
        shiftCode = "",
        fuelLiters = 0.0,
        status = WeightedRosterTierStatus.PROPOSED,
    )
}
