package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.text.normalize

/** Where a pending service area assignment is in its life cycle. */
enum class PendingServiceAreaAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending service area assignment. Its code is unique within a tenant. */
data class PendingServiceAreaAssignment(
    var code: String,
    var title: String,
    var summary: String,
    var phone: String,
    var minimum: Double,
    var status: PendingServiceAreaAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServiceAreaAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServiceAreaAssignmentStatus): Boolean {
        when (status) {
            PendingServiceAreaAssignmentStatus.OPEN -> {
                when (next) {
                    PendingServiceAreaAssignmentStatus.IN_REVIEW, PendingServiceAreaAssignmentStatus.RESOLVED -> return true
                }
                return false
            }
            PendingServiceAreaAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    PendingServiceAreaAssignmentStatus.RESOLVED, PendingServiceAreaAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingServiceAreaAssignmentStatus.RESOLVED -> return next == PendingServiceAreaAssignmentStatus.REOPENED
            PendingServiceAreaAssignmentStatus.REOPENED -> {
                when (next) {
                    PendingServiceAreaAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingServiceAreaAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending service area assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending service area assignment"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): PendingServiceAreaAssignment {
        return PendingServiceAreaAssignment(
            code = code,
            title = title,
            summary = summary,
            phone = phone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending service area assignment in status open. */
fun newPendingServiceAreaAssignment(code: String, title: String): PendingServiceAreaAssignment {
    return PendingServiceAreaAssignment(
        code = code,
        title = title,
        summary = "",
        phone = "",
        minimum = 0.0,
        status = PendingServiceAreaAssignmentStatus.OPEN,
    )
}
