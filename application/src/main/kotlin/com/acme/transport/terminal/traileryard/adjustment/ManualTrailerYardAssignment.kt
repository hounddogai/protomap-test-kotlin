package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.text.normalize

/** Where a manual trailer yard assignment is in its life cycle. */
enum class ManualTrailerYardAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One manual trailer yard assignment, with what the planners need to act on it. */
data class ManualTrailerYardAssignment(
    var code: String,
    var title: String,
    var category: String,
    var reviewer: String,
    var minimum: Double,
    var status: ManualTrailerYardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerYardAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerYardAssignmentStatus): Boolean {
        when (status) {
            ManualTrailerYardAssignmentStatus.IDLE -> {
                when (next) {
                    ManualTrailerYardAssignmentStatus.READY, ManualTrailerYardAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            ManualTrailerYardAssignmentStatus.WARMING -> {
                when (next) {
                    ManualTrailerYardAssignmentStatus.READY -> return true
                }
                return false
            }
            ManualTrailerYardAssignmentStatus.READY -> {
                when (next) {
                    ManualTrailerYardAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            ManualTrailerYardAssignmentStatus.DRAINING -> return next == ManualTrailerYardAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualTrailerYardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer yard assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = minimum

    fun snapshot(): ManualTrailerYardAssignment {
        return ManualTrailerYardAssignment(
            code = code,
            title = title,
            category = category,
            reviewer = reviewer,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual trailer yard assignment in status idle. */
fun newManualTrailerYardAssignment(code: String, title: String): ManualTrailerYardAssignment {
    return ManualTrailerYardAssignment(
        code = code,
        title = title,
        category = "",
        reviewer = "",
        minimum = 0.0,
        status = ManualTrailerYardAssignmentStatus.IDLE,
    )
}
