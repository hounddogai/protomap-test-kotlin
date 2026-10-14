package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted cross dock slot records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a weighted cross dock slot is in its life cycle. */
enum class WeightedCrossDockSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted cross dock slot and its current status. */
data class WeightedCrossDockSlot(
    var code: String,
    var name: String,
    var margin: Double,
    var odometer: Int,
    var summary: String,
    var graceDays: Int,
    var status: WeightedCrossDockSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCrossDockSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCrossDockSlotStatus): Boolean {
        when (status) {
            WeightedCrossDockSlotStatus.PLANNED -> {
                when (next) {
                    WeightedCrossDockSlotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            WeightedCrossDockSlotStatus.SCHEDULED -> return next == WeightedCrossDockSlotStatus.COMPLETED
            WeightedCrossDockSlotStatus.IN_PROGRESS -> return next == WeightedCrossDockSlotStatus.CANCELLED
            WeightedCrossDockSlotStatus.COMPLETED -> {
                when (next) {
                    WeightedCrossDockSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCrossDockSlotStatus) {
        if (!canMoveTo(next)) {
            error("weighted cross dock slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * margin * 100) / 100

    fun snapshot(): WeightedCrossDockSlot {
        return WeightedCrossDockSlot(
            code = code,
            name = name,
            margin = margin,
            odometer = odometer,
            summary = summary,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted cross dock slot in status planned. */
fun newWeightedCrossDockSlot(code: String, name: String): WeightedCrossDockSlot {
    return WeightedCrossDockSlot(
        code = code,
        name = name,
        margin = 0.0,
        odometer = 0,
        summary = "",
        graceDays = 0,
        status = WeightedCrossDockSlotStatus.PLANNED,
    )
}
