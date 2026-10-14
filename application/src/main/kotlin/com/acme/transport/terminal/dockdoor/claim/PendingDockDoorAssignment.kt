package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.text.normalize

/** Where a pending dock door assignment is in its life cycle. */
enum class PendingDockDoorAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending dock door assignment, with what the planners need to act on it. */
data class PendingDockDoorAssignment(
    var code: String,
    var name: String,
    var score: Double,
    var ownerId: String,
    var dock: String,
    var status: PendingDockDoorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDockDoorAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDockDoorAssignmentStatus): Boolean {
        when (status) {
            PendingDockDoorAssignmentStatus.REQUESTED -> return next == PendingDockDoorAssignmentStatus.GRANTED || next == PendingDockDoorAssignmentStatus.REVOKED
            PendingDockDoorAssignmentStatus.GRANTED -> {
                when (next) {
                    PendingDockDoorAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            PendingDockDoorAssignmentStatus.REVOKED -> return next == PendingDockDoorAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDockDoorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending dock door assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = score

    fun snapshot(): PendingDockDoorAssignment {
        return PendingDockDoorAssignment(
            code = code,
            name = name,
            score = score,
            ownerId = ownerId,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending dock door assignment in status requested. */
fun newPendingDockDoorAssignment(code: String, name: String): PendingDockDoorAssignment {
    return PendingDockDoorAssignment(
        code = code,
        name = name,
        score = 0.0,
        ownerId = "",
        dock = "",
        status = PendingDockDoorAssignmentStatus.REQUESTED,
    )
}
