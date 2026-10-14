package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door window records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door window is in its life cycle. */
enum class DockDoorWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dock door window as the dock door team keeps it. */
data class DockDoorWindow(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var reviewer: String,
    var label: String,
    var floorValue: Double,
    var retries: Int,
    var ceiling: Double,
    var notes: String,
    var dock: String,
    var endsAt: Int,
    var status: DockDoorWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorWindowStatus): Boolean {
        when (status) {
            DockDoorWindowStatus.OPEN -> return next == DockDoorWindowStatus.ARCHIVED
            DockDoorWindowStatus.IN_REVIEW -> {
                when (next) {
                    DockDoorWindowStatus.REOPENED -> return true
                }
                return false
            }
            DockDoorWindowStatus.RESOLVED -> {
                when (next) {
                    DockDoorWindowStatus.REOPENED -> return true
                }
                return false
            }
            DockDoorWindowStatus.REOPENED -> {
                when (next) {
                    DockDoorWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorWindowStatus) {
        if (!canMoveTo(next)) {
            error("dock door window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun netExposure(): Double = round((ceiling + floorValue) * 100) / 100

    fun snapshot(): DockDoorWindow {
        return DockDoorWindow(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            reviewer = reviewer,
            label = label,
            floorValue = floorValue,
            retries = retries,
            ceiling = ceiling,
            notes = notes,
            dock = dock,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door window in status open. */
fun newDockDoorWindow(code: String, name: String): DockDoorWindow {
    return DockDoorWindow(
        code = code,
        name = name,
        fuelLiters = 0.0,
        reviewer = "",
        label = "",
        floorValue = 0.0,
        retries = 0,
        ceiling = 0.0,
        notes = "",
        dock = "",
        endsAt = 0,
        status = DockDoorWindowStatus.OPEN,
    )
}
