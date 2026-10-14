package com.acme.transport.terminal.dockdoor.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door reservation records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a dock door reservation is in its life cycle. */
enum class DockDoorReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dock door reservation as the dock door team keeps it. */
data class DockDoorReservation(
    var code: String,
    var name: String,
    var title: String,
    var tags: MutableList<String>,
    var approved: Boolean,
    var priority: Int,
    var odometer: Int,
    var status: DockDoorReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorReservationStatus): Boolean {
        when (status) {
            DockDoorReservationStatus.IDLE -> {
                when (next) {
                    DockDoorReservationStatus.READY, DockDoorReservationStatus.STOPPED -> return true
                }
                return false
            }
            DockDoorReservationStatus.WARMING -> {
                when (next) {
                    DockDoorReservationStatus.READY -> return true
                }
                return false
            }
            DockDoorReservationStatus.READY -> return next == DockDoorReservationStatus.DRAINING || next == DockDoorReservationStatus.STOPPED
            DockDoorReservationStatus.DRAINING -> {
                when (next) {
                    DockDoorReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorReservationStatus) {
        if (!canMoveTo(next)) {
            error("dock door reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((odometer.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): DockDoorReservation {
        return DockDoorReservation(
            code = code,
            name = name,
            title = title,
            tags = tags,
            approved = approved,
            priority = priority,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door reservation in status idle. */
fun newDockDoorReservation(code: String, name: String): DockDoorReservation {
    return DockDoorReservation(
        code = code,
        name = name,
        title = "",
        tags = mutableListOf<String>(),
        approved = false,
        priority = 0,
        odometer = 0,
        status = DockDoorReservationStatus.IDLE,
    )
}
