package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock assignment records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock assignment is in its life cycle. */
enum class CrossDockAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One cross dock assignment, with what the planners need to act on it. */
data class CrossDockAssignment(
    var code: String,
    var name: String,
    var locale: String,
    var active: Boolean,
    var threshold: Double,
    var title: String,
    var stopCount: Int,
    var minimum: Double,
    var status: CrossDockAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockAssignmentStatus): Boolean {
        when (status) {
            CrossDockAssignmentStatus.REQUESTED -> {
                when (next) {
                    CrossDockAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            CrossDockAssignmentStatus.GRANTED -> {
                when (next) {
                    CrossDockAssignmentStatus.REVOKED, CrossDockAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            CrossDockAssignmentStatus.REVOKED -> return next == CrossDockAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CrossDockAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("cross dock assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cross dock assignment"} $code"

    fun weightedExposure(): Double = round(threshold * stopCount.toDouble() * 100) / 100

    fun snapshot(): CrossDockAssignment {
        return CrossDockAssignment(
            code = code,
            name = name,
            locale = locale,
            active = active,
            threshold = threshold,
            title = title,
            stopCount = stopCount,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a cross dock assignment in status requested. */
fun newCrossDockAssignment(code: String, name: String): CrossDockAssignment {
    return CrossDockAssignment(
        code = code,
        name = name,
        locale = "",
        active = false,
        threshold = 0.0,
        title = "",
        stopCount = 0,
        minimum = 0.0,
        status = CrossDockAssignmentStatus.REQUESTED,
    )
}
