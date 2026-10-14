package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard assignment records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a trailer yard assignment is in its life cycle. */
enum class TrailerYardAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One trailer yard assignment, with what the planners need to act on it. */
data class TrailerYardAssignment(
    var code: String,
    var label: String,
    var threshold: Double,
    var visible: Boolean,
    var stopCount: Int,
    var segment: String,
    var ownerId: String,
    var source: String,
    var reviewer: String,
    var attempts: Int,
    var status: TrailerYardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardAssignmentStatus): Boolean {
        when (status) {
            TrailerYardAssignmentStatus.NEW -> return next == TrailerYardAssignmentStatus.DONE
            TrailerYardAssignmentStatus.QUEUED -> {
                when (next) {
                    TrailerYardAssignmentStatus.RUNNING, TrailerYardAssignmentStatus.DONE -> return true
                }
                return false
            }
            TrailerYardAssignmentStatus.RUNNING -> return next == TrailerYardAssignmentStatus.DONE
            TrailerYardAssignmentStatus.DONE -> return next == TrailerYardAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TrailerYardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"trailer yard assignment"} $code"

    fun projectedTotal(): Double = round((stopCount.toDouble() - threshold) * 100) / 100

    fun snapshot(): TrailerYardAssignment {
        return TrailerYardAssignment(
            code = code,
            label = label,
            threshold = threshold,
            visible = visible,
            stopCount = stopCount,
            segment = segment,
            ownerId = ownerId,
            source = source,
            reviewer = reviewer,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer yard assignment in status new. */
fun newTrailerYardAssignment(code: String, label: String): TrailerYardAssignment {
    return TrailerYardAssignment(
        code = code,
        label = label,
        threshold = 0.0,
        visible = false,
        stopCount = 0,
        segment = "",
        ownerId = "",
        source = "",
        reviewer = "",
        attempts = 0,
        status = TrailerYardAssignmentStatus.NEW,
    )
}
