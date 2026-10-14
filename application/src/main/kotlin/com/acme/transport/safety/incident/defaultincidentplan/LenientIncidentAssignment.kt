package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.text.normalize

/** Where a lenient incident assignment is in its life cycle. */
enum class LenientIncidentAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient incident assignment. Its code is unique within a tenant. */
data class LenientIncidentAssignment(
    var code: String,
    var label: String,
    var approved: Boolean,
    var graceDays: Int,
    var tags: MutableList<String>,
    var status: LenientIncidentAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIncidentAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIncidentAssignmentStatus): Boolean {
        when (status) {
            LenientIncidentAssignmentStatus.OPEN -> {
                when (next) {
                    LenientIncidentAssignmentStatus.IN_REVIEW, LenientIncidentAssignmentStatus.RESOLVED -> return true
                }
                return false
            }
            LenientIncidentAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    LenientIncidentAssignmentStatus.RESOLVED, LenientIncidentAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientIncidentAssignmentStatus.RESOLVED -> {
                when (next) {
                    LenientIncidentAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientIncidentAssignmentStatus.REOPENED -> return next == LenientIncidentAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientIncidentAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient incident assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): LenientIncidentAssignment {
        return LenientIncidentAssignment(
            code = code,
            label = label,
            approved = approved,
            graceDays = graceDays,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient incident assignment in status open. */
fun newLenientIncidentAssignment(code: String, label: String): LenientIncidentAssignment {
    return LenientIncidentAssignment(
        code = code,
        label = label,
        approved = false,
        graceDays = 0,
        tags = mutableListOf<String>(),
        status = LenientIncidentAssignmentStatus.OPEN,
    )
}
