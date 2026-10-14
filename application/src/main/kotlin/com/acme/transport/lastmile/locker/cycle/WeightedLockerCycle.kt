package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted locker cycle records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a weighted locker cycle is in its life cycle. */
enum class WeightedLockerCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted locker cycle as the locker team keeps it. */
data class WeightedLockerCycle(
    var code: String,
    var name: String,
    var ownerId: String,
    var segment: String,
    var fuelLiters: Double,
    var distanceKm: Double,
    var lane: String,
    var margin: Double,
    var status: WeightedLockerCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLockerCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLockerCycleStatus): Boolean {
        when (status) {
            WeightedLockerCycleStatus.PLANNED -> return next == WeightedLockerCycleStatus.IN_PROGRESS
            WeightedLockerCycleStatus.SCHEDULED -> return next == WeightedLockerCycleStatus.CANCELLED
            WeightedLockerCycleStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedLockerCycleStatus.COMPLETED, WeightedLockerCycleStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedLockerCycleStatus.COMPLETED -> {
                when (next) {
                    WeightedLockerCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLockerCycleStatus) {
        if (!canMoveTo(next)) {
            error("weighted locker cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((fuelLiters + margin) * 100) / 100

    fun snapshot(): WeightedLockerCycle {
        return WeightedLockerCycle(
            code = code,
            name = name,
            ownerId = ownerId,
            segment = segment,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            lane = lane,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted locker cycle in status planned. */
fun newWeightedLockerCycle(code: String, name: String): WeightedLockerCycle {
    return WeightedLockerCycle(
        code = code,
        name = name,
        ownerId = "",
        segment = "",
        fuelLiters = 0.0,
        distanceKm = 0.0,
        lane = "",
        margin = 0.0,
        status = WeightedLockerCycleStatus.PLANNED,
    )
}
