package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel burn target is in its life cycle. */
enum class WeightedFuelBurnTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted fuel burn target as the fuel burn team keeps it. */
data class WeightedFuelBurnTarget(
    var code: String,
    var name: String,
    var ceiling: Double,
    var depot: String,
    var odometer: Int,
    var createdAt: Int,
    var shiftCode: String,
    var status: WeightedFuelBurnTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnTargetStatus): Boolean {
        when (status) {
            WeightedFuelBurnTargetStatus.IDLE -> return next == WeightedFuelBurnTargetStatus.READY
            WeightedFuelBurnTargetStatus.WARMING -> return next == WeightedFuelBurnTargetStatus.DRAINING
            WeightedFuelBurnTargetStatus.READY -> return next == WeightedFuelBurnTargetStatus.DRAINING || next == WeightedFuelBurnTargetStatus.STOPPED
            WeightedFuelBurnTargetStatus.DRAINING -> return next == WeightedFuelBurnTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round(odometer.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): WeightedFuelBurnTarget {
        return WeightedFuelBurnTarget(
            code = code,
            name = name,
            ceiling = ceiling,
            depot = depot,
            odometer = odometer,
            createdAt = createdAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted fuel burn target in status idle. */
fun newWeightedFuelBurnTarget(code: String, name: String): WeightedFuelBurnTarget {
    return WeightedFuelBurnTarget(
        code = code,
        name = name,
        ceiling = 0.0,
        depot = "",
        odometer = 0,
        createdAt = 0,
        shiftCode = "",
        status = WeightedFuelBurnTargetStatus.IDLE,
    )
}
