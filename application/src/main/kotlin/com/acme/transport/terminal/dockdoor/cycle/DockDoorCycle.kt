package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.normalize

// The dock door cycle records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door cycle is in its life cycle. */
enum class DockDoorCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A dock door cycle as the dock door team keeps it. */
data class DockDoorCycle(
    var code: String,
    var title: String,
    var channel: String,
    var active: Boolean,
    var summary: String,
    var archived: Boolean,
    var loadKg: Double,
    var lane: String,
    var status: DockDoorCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorCycleStatus): Boolean {
        when (status) {
            DockDoorCycleStatus.DRAFT -> {
                when (next) {
                    DockDoorCycleStatus.PENDING, DockDoorCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            DockDoorCycleStatus.PENDING -> return next == DockDoorCycleStatus.ACTIVE || next == DockDoorCycleStatus.SUSPENDED
            DockDoorCycleStatus.ACTIVE -> return next == DockDoorCycleStatus.SUSPENDED
            DockDoorCycleStatus.SUSPENDED -> return next == DockDoorCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DockDoorCycleStatus) {
        if (!canMoveTo(next)) {
            error("dock door cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door cycle"} $code"

    fun measuredValue(): Double = loadKg

    fun snapshot(): DockDoorCycle {
        return DockDoorCycle(
            code = code,
            title = title,
            channel = channel,
            active = active,
            summary = summary,
            archived = archived,
            loadKg = loadKg,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dock door cycle in status draft. */
fun newDockDoorCycle(code: String, title: String): DockDoorCycle {
    return DockDoorCycle(
        code = code,
        title = title,
        channel = "",
        active = false,
        summary = "",
        archived = false,
        loadKg = 0.0,
        lane = "",
        status = DockDoorCycleStatus.DRAFT,
    )
}
