package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.text.normalize

/** Where a roster assignment is in its life cycle. */
enum class RosterAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One roster assignment, with what the planners need to act on it. */
data class RosterAssignment(
    var code: String,
    var name: String,
    var priority: Int,
    var attributes: MutableMap<String, String>,
    var label: String,
    var trailerId: String,
    var status: RosterAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterAssignmentStatus): Boolean {
        when (status) {
            RosterAssignmentStatus.PROPOSED -> return next == RosterAssignmentStatus.APPROVED || next == RosterAssignmentStatus.WITHDRAWN
            RosterAssignmentStatus.APPROVED -> return next == RosterAssignmentStatus.REJECTED || next == RosterAssignmentStatus.WITHDRAWN
            RosterAssignmentStatus.REJECTED -> return next == RosterAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RosterAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("roster assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): RosterAssignment {
        return RosterAssignment(
            code = code,
            name = name,
            priority = priority,
            attributes = attributes,
            label = label,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster assignment in status proposed. */
fun newRosterAssignment(code: String, name: String): RosterAssignment {
    return RosterAssignment(
        code = code,
        name = name,
        priority = 0,
        attributes = mutableMapOf<String, String>(),
        label = "",
        trailerId = "",
        status = RosterAssignmentStatus.PROPOSED,
    )
}
