package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker assignment records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a locker assignment is in its life cycle. */
enum class LockerAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a locker assignment and its current status. */
data class LockerAssignment(
    var code: String,
    var name: String,
    var startsAt: Int,
    var sequence: Int,
    var attempts: Int,
    var category: String,
    var ownerId: String,
    var approved: Boolean,
    var archived: Boolean,
    var checksum: String,
    var status: LockerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerAssignmentStatus): Boolean {
        when (status) {
            LockerAssignmentStatus.CREATED -> return next == LockerAssignmentStatus.VALIDATED
            LockerAssignmentStatus.VALIDATED -> {
                when (next) {
                    LockerAssignmentStatus.PUBLISHED, LockerAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            LockerAssignmentStatus.PUBLISHED -> return next == LockerAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LockerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("locker assignment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(attempts.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): LockerAssignment {
        return LockerAssignment(
            code = code,
            name = name,
            startsAt = startsAt,
            sequence = sequence,
            attempts = attempts,
            category = category,
            ownerId = ownerId,
            approved = approved,
            archived = archived,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a locker assignment in status created. */
fun newLockerAssignment(code: String, name: String): LockerAssignment {
    return LockerAssignment(
        code = code,
        name = name,
        startsAt = 0,
        sequence = 0,
        attempts = 0,
        category = "",
        ownerId = "",
        approved = false,
        archived = false,
        checksum = "",
        status = LockerAssignmentStatus.CREATED,
    )
}
