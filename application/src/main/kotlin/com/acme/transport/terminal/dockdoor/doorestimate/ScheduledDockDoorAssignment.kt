package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dock door assignment is in its life cycle. */
enum class ScheduledDockDoorAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled dock door assignment and its current status. */
data class ScheduledDockDoorAssignment(
    var code: String,
    var label: String,
    var shiftCode: String,
    var pallets: Int,
    var capacity: Int,
    var quantity: Int,
    var status: ScheduledDockDoorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorAssignmentStatus): Boolean {
        when (status) {
            ScheduledDockDoorAssignmentStatus.NEW -> return next == ScheduledDockDoorAssignmentStatus.FAILED
            ScheduledDockDoorAssignmentStatus.QUEUED -> return next == ScheduledDockDoorAssignmentStatus.RUNNING || next == ScheduledDockDoorAssignmentStatus.DONE
            ScheduledDockDoorAssignmentStatus.RUNNING -> return next == ScheduledDockDoorAssignmentStatus.FAILED
            ScheduledDockDoorAssignmentStatus.DONE -> return next == ScheduledDockDoorAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled dock door assignment"} $code"

    fun projectedLoad(): Double = round((capacity.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDockDoorAssignment {
        return ScheduledDockDoorAssignment(
            code = code,
            label = label,
            shiftCode = shiftCode,
            pallets = pallets,
            capacity = capacity,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled dock door assignment in status new. */
fun newScheduledDockDoorAssignment(code: String, label: String): ScheduledDockDoorAssignment {
    return ScheduledDockDoorAssignment(
        code = code,
        label = label,
        shiftCode = "",
        pallets = 0,
        capacity = 0,
        quantity = 0,
        status = ScheduledDockDoorAssignmentStatus.NEW,
    )
}
