package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.normalize

// The scheduled cross dock slot records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a scheduled cross dock slot is in its life cycle. */
enum class ScheduledCrossDockSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled cross dock slot and its current status. */
data class ScheduledCrossDockSlot(
    var code: String,
    var name: String,
    var notes: String,
    var ratio: Double,
    var region: String,
    var driverId: String,
    var description: String,
    var dock: String,
    var status: ScheduledCrossDockSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCrossDockSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCrossDockSlotStatus): Boolean {
        when (status) {
            ScheduledCrossDockSlotStatus.CREATED -> return next == ScheduledCrossDockSlotStatus.RETIRED
            ScheduledCrossDockSlotStatus.VALIDATED -> {
                when (next) {
                    ScheduledCrossDockSlotStatus.PUBLISHED, ScheduledCrossDockSlotStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledCrossDockSlotStatus.PUBLISHED -> return next == ScheduledCrossDockSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledCrossDockSlotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cross dock slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = ratio

    fun snapshot(): ScheduledCrossDockSlot {
        return ScheduledCrossDockSlot(
            code = code,
            name = name,
            notes = notes,
            ratio = ratio,
            region = region,
            driverId = driverId,
            description = description,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled cross dock slot in status created. */
fun newScheduledCrossDockSlot(code: String, name: String): ScheduledCrossDockSlot {
    return ScheduledCrossDockSlot(
        code = code,
        name = name,
        notes = "",
        ratio = 0.0,
        region = "",
        driverId = "",
        description = "",
        dock = "",
        status = ScheduledCrossDockSlotStatus.CREATED,
    )
}
