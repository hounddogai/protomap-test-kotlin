package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled swap slot is in its life cycle. */
enum class ScheduledSwapSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled swap slot as the swap team keeps it. */
data class ScheduledSwapSlot(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var active: Boolean,
    var carrier: String,
    var capacity: Int,
    var floorValue: Double,
    var status: ScheduledSwapSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSwapSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSwapSlotStatus): Boolean {
        when (status) {
            ScheduledSwapSlotStatus.IDLE -> {
                when (next) {
                    ScheduledSwapSlotStatus.DRAINING, ScheduledSwapSlotStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledSwapSlotStatus.WARMING -> return next == ScheduledSwapSlotStatus.READY
            ScheduledSwapSlotStatus.READY -> {
                when (next) {
                    ScheduledSwapSlotStatus.DRAINING, ScheduledSwapSlotStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledSwapSlotStatus.DRAINING -> {
                when (next) {
                    ScheduledSwapSlotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSwapSlotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled swap slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((fuelLiters - capacity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledSwapSlot {
        return ScheduledSwapSlot(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            active = active,
            carrier = carrier,
            capacity = capacity,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled swap slot in status idle. */
fun newScheduledSwapSlot(code: String, name: String): ScheduledSwapSlot {
    return ScheduledSwapSlot(
        code = code,
        name = name,
        fuelLiters = 0.0,
        active = false,
        carrier = "",
        capacity = 0,
        floorValue = 0.0,
        status = ScheduledSwapSlotStatus.IDLE,
    )
}
