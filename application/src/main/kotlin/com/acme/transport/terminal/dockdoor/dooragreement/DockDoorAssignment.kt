package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door assignment is in its life cycle. */
enum class DockDoorAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One dock door assignment, with what the planners need to act on it. */
data class DockDoorAssignment(
    var code: String,
    var name: String,
    var label: String,
    var expiresAt: Int,
    var description: String,
    var updatedAt: Int,
    var etaMinutes: Int,
    var status: DockDoorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorAssignmentStatus): Boolean {
        when (status) {
            DockDoorAssignmentStatus.PROPOSED -> return next == DockDoorAssignmentStatus.APPROVED
            DockDoorAssignmentStatus.APPROVED -> {
                when (next) {
                    DockDoorAssignmentStatus.REJECTED -> return true
                }
                return false
            }
            DockDoorAssignmentStatus.REJECTED -> return next == DockDoorAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DockDoorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("dock door assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun projectedValue(): Double = round(expiresAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): DockDoorAssignment {
        return DockDoorAssignment(
            code = code,
            name = name,
            label = label,
            expiresAt = expiresAt,
            description = description,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door assignment in status proposed. */
fun newDockDoorAssignment(code: String, name: String): DockDoorAssignment {
    return DockDoorAssignment(
        code = code,
        name = name,
        label = "",
        expiresAt = 0,
        description = "",
        updatedAt = 0,
        etaMinutes = 0,
        status = DockDoorAssignmentStatus.PROPOSED,
    )
}
