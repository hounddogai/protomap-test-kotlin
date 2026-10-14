package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation assignment is in its life cycle. */
enum class SortationAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One sortation assignment, with what the planners need to act on it. */
data class SortationAssignment(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var odometer: Int,
    var attempts: Int,
    var status: SortationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationAssignmentStatus): Boolean {
        when (status) {
            SortationAssignmentStatus.IDLE -> {
                when (next) {
                    SortationAssignmentStatus.WARMING, SortationAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            SortationAssignmentStatus.WARMING -> return next == SortationAssignmentStatus.READY || next == SortationAssignmentStatus.DRAINING
            SortationAssignmentStatus.READY -> {
                when (next) {
                    SortationAssignmentStatus.DRAINING, SortationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            SortationAssignmentStatus.DRAINING -> {
                when (next) {
                    SortationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("sortation assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"sortation assignment"} $code"

    fun weightedValue(): Double = round(attempts.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): SortationAssignment {
        return SortationAssignment(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            odometer = odometer,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a sortation assignment in status idle. */
fun newSortationAssignment(code: String, title: String): SortationAssignment {
    return SortationAssignment(
        code = code,
        title = title,
        etaMinutes = 0,
        odometer = 0,
        attempts = 0,
        status = SortationAssignmentStatus.IDLE,
    )
}
