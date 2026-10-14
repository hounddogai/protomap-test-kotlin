package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold assignment records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold assignment is in its life cycle. */
enum class HoldAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One hold assignment, with what the planners need to act on it. */
data class HoldAssignment(
    var code: String,
    var title: String,
    var carrier: String,
    var updatedAt: Int,
    var checksum: String,
    var priority: Int,
    var score: Double,
    var status: HoldAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldAssignmentStatus): Boolean {
        when (status) {
            HoldAssignmentStatus.DRAFT -> {
                when (next) {
                    HoldAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            HoldAssignmentStatus.PENDING -> return next == HoldAssignmentStatus.ACTIVE || next == HoldAssignmentStatus.SUSPENDED
            HoldAssignmentStatus.ACTIVE -> {
                when (next) {
                    HoldAssignmentStatus.SUSPENDED, HoldAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            HoldAssignmentStatus.SUSPENDED -> {
                when (next) {
                    HoldAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("hold assignment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((score - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): HoldAssignment {
        return HoldAssignment(
            code = code,
            title = title,
            carrier = carrier,
            updatedAt = updatedAt,
            checksum = checksum,
            priority = priority,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold assignment in status draft. */
fun newHoldAssignment(code: String, title: String): HoldAssignment {
    return HoldAssignment(
        code = code,
        title = title,
        carrier = "",
        updatedAt = 0,
        checksum = "",
        priority = 0,
        score = 0.0,
        status = HoldAssignmentStatus.DRAFT,
    )
}
