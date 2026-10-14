package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted availability group is in its life cycle. */
enum class WeightedAvailabilityGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted availability group as the availability team keeps it. */
data class WeightedAvailabilityGroup(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var fuelLiters: Double,
    var trailerId: String,
    var maximum: Double,
    var status: WeightedAvailabilityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAvailabilityGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAvailabilityGroupStatus): Boolean {
        when (status) {
            WeightedAvailabilityGroupStatus.IDLE -> {
                when (next) {
                    WeightedAvailabilityGroupStatus.WARMING -> return true
                }
                return false
            }
            WeightedAvailabilityGroupStatus.WARMING -> return next == WeightedAvailabilityGroupStatus.READY || next == WeightedAvailabilityGroupStatus.DRAINING
            WeightedAvailabilityGroupStatus.READY -> return next == WeightedAvailabilityGroupStatus.DRAINING || next == WeightedAvailabilityGroupStatus.STOPPED
            WeightedAvailabilityGroupStatus.DRAINING -> return next == WeightedAvailabilityGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedAvailabilityGroupStatus) {
        if (!canMoveTo(next)) {
            error("weighted availability group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round(expiresAt.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): WeightedAvailabilityGroup {
        return WeightedAvailabilityGroup(
            code = code,
            name = name,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted availability group in status idle. */
fun newWeightedAvailabilityGroup(code: String, name: String): WeightedAvailabilityGroup {
    return WeightedAvailabilityGroup(
        code = code,
        name = name,
        expiresAt = 0,
        fuelLiters = 0.0,
        trailerId = "",
        maximum = 0.0,
        status = WeightedAvailabilityGroupStatus.IDLE,
    )
}
