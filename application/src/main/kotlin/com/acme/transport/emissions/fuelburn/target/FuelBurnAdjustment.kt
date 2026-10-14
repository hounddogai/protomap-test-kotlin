package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn adjustment is in its life cycle. */
enum class FuelBurnAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel burn adjustment, with what the planners need to act on it. */
data class FuelBurnAdjustment(
    var code: String,
    var title: String,
    var odometer: Int,
    var archived: Boolean,
    var ceiling: Double,
    var weight: Double,
    var attributes: MutableMap<String, String>,
    var status: FuelBurnAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnAdjustmentStatus): Boolean {
        when (status) {
            FuelBurnAdjustmentStatus.IDLE -> {
                when (next) {
                    FuelBurnAdjustmentStatus.READY -> return true
                }
                return false
            }
            FuelBurnAdjustmentStatus.WARMING -> {
                when (next) {
                    FuelBurnAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            FuelBurnAdjustmentStatus.READY -> {
                when (next) {
                    FuelBurnAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            FuelBurnAdjustmentStatus.DRAINING -> {
                when (next) {
                    FuelBurnAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round(weight * ceiling * 100) / 100

    fun snapshot(): FuelBurnAdjustment {
        return FuelBurnAdjustment(
            code = code,
            title = title,
            odometer = odometer,
            archived = archived,
            ceiling = ceiling,
            weight = weight,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn adjustment in status idle. */
fun newFuelBurnAdjustment(code: String, title: String): FuelBurnAdjustment {
    return FuelBurnAdjustment(
        code = code,
        title = title,
        odometer = 0,
        archived = false,
        ceiling = 0.0,
        weight = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = FuelBurnAdjustmentStatus.IDLE,
    )
}
