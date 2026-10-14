package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock slot records of terminal's cross dock work, and the rules, stores, and calculations around
// them.

/** Where a cross dock slot is in its life cycle. */
enum class CrossDockSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One cross dock slot, with what the planners need to act on it. */
data class CrossDockSlot(
    var code: String,
    var label: String,
    var vehicleId: String,
    var lane: String,
    var revision: Int,
    var reviewer: String,
    var priority: Int,
    var status: CrossDockSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockSlotStatus): Boolean {
        when (status) {
            CrossDockSlotStatus.NEW -> return next == CrossDockSlotStatus.QUEUED || next == CrossDockSlotStatus.RUNNING
            CrossDockSlotStatus.QUEUED -> return next == CrossDockSlotStatus.RUNNING
            CrossDockSlotStatus.RUNNING -> {
                when (next) {
                    CrossDockSlotStatus.DONE, CrossDockSlotStatus.FAILED -> return true
                }
                return false
            }
            CrossDockSlotStatus.DONE -> {
                when (next) {
                    CrossDockSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockSlotStatus) {
        if (!canMoveTo(next)) {
            error("cross dock slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): CrossDockSlot {
        return CrossDockSlot(
            code = code,
            label = label,
            vehicleId = vehicleId,
            lane = lane,
            revision = revision,
            reviewer = reviewer,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a cross dock slot in status new. */
fun newCrossDockSlot(code: String, label: String): CrossDockSlot {
    return CrossDockSlot(
        code = code,
        label = label,
        vehicleId = "",
        lane = "",
        revision = 0,
        reviewer = "",
        priority = 0,
        status = CrossDockSlotStatus.NEW,
    )
}
