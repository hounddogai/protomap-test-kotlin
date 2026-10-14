package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell slot is in its life cycle. */
enum class DwellSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One dwell slot, with what the planners need to act on it. */
data class DwellSlot(
    var code: String,
    var name: String,
    var odometer: Int,
    var budget: Double,
    var description: String,
    var startsAt: Int,
    var source: String,
    var depot: String,
    var lane: String,
    var quantity: Int,
    var status: DwellSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellSlotStatus): Boolean {
        when (status) {
            DwellSlotStatus.NEW -> {
                when (next) {
                    DwellSlotStatus.FAILED -> return true
                }
                return false
            }
            DwellSlotStatus.QUEUED -> {
                when (next) {
                    DwellSlotStatus.RUNNING, DwellSlotStatus.DONE -> return true
                }
                return false
            }
            DwellSlotStatus.RUNNING -> {
                when (next) {
                    DwellSlotStatus.DONE, DwellSlotStatus.FAILED -> return true
                }
                return false
            }
            DwellSlotStatus.DONE -> return next == DwellSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DwellSlotStatus) {
        if (!canMoveTo(next)) {
            error("dwell slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dwell slot"} $code"

    fun netLoad(): Double = round((budget - startsAt.toDouble()) * 100) / 100

    fun snapshot(): DwellSlot {
        return DwellSlot(
            code = code,
            name = name,
            odometer = odometer,
            budget = budget,
            description = description,
            startsAt = startsAt,
            source = source,
            depot = depot,
            lane = lane,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dwell slot in status new. */
fun newDwellSlot(code: String, name: String): DwellSlot {
    return DwellSlot(
        code = code,
        name = name,
        odometer = 0,
        budget = 0.0,
        description = "",
        startsAt = 0,
        source = "",
        depot = "",
        lane = "",
        quantity = 0,
        status = DwellSlotStatus.NEW,
    )
}
