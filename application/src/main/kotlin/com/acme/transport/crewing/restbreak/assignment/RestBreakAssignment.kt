package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break assignment records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break assignment is in its life cycle. */
enum class RestBreakAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rest break assignment. Its code is unique within a tenant. */
data class RestBreakAssignment(
    var code: String,
    var label: String,
    var approved: Boolean,
    var pinned: Boolean,
    var version: Int,
    var limit: Int,
    var status: RestBreakAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakAssignmentStatus): Boolean {
        when (status) {
            RestBreakAssignmentStatus.OPEN -> return next == RestBreakAssignmentStatus.REOPENED || next == RestBreakAssignmentStatus.ARCHIVED
            RestBreakAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    RestBreakAssignmentStatus.RESOLVED, RestBreakAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            RestBreakAssignmentStatus.RESOLVED -> {
                when (next) {
                    RestBreakAssignmentStatus.REOPENED, RestBreakAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            RestBreakAssignmentStatus.REOPENED -> {
                when (next) {
                    RestBreakAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("rest break assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round((limit.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RestBreakAssignment {
        return RestBreakAssignment(
            code = code,
            label = label,
            approved = approved,
            pinned = pinned,
            version = version,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break assignment in status open. */
fun newRestBreakAssignment(code: String, label: String): RestBreakAssignment {
    return RestBreakAssignment(
        code = code,
        label = label,
        approved = false,
        pinned = false,
        version = 0,
        limit = 0,
        status = RestBreakAssignmentStatus.OPEN,
    )
}
