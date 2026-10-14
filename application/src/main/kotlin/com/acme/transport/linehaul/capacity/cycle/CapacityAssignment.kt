package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity assignment is in its life cycle. */
enum class CapacityAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capacity assignment and its current status. */
data class CapacityAssignment(
    var code: String,
    var name: String,
    var threshold: Double,
    var lane: String,
    var score: Double,
    var expiresAt: Int,
    var status: CapacityAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityAssignmentStatus): Boolean {
        when (status) {
            CapacityAssignmentStatus.REQUESTED -> {
                when (next) {
                    CapacityAssignmentStatus.GRANTED, CapacityAssignmentStatus.REVOKED -> return true
                }
                return false
            }
            CapacityAssignmentStatus.GRANTED -> return next == CapacityAssignmentStatus.REVOKED || next == CapacityAssignmentStatus.EXPIRED
            CapacityAssignmentStatus.REVOKED -> return next == CapacityAssignmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CapacityAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capacity assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(score * expiresAt.toDouble() * 100) / 100

    fun snapshot(): CapacityAssignment {
        return CapacityAssignment(
            code = code,
            name = name,
            threshold = threshold,
            lane = lane,
            score = score,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capacity assignment in status requested. */
fun newCapacityAssignment(code: String, name: String): CapacityAssignment {
    return CapacityAssignment(
        code = code,
        name = name,
        threshold = 0.0,
        lane = "",
        score = 0.0,
        expiresAt = 0,
        status = CapacityAssignmentStatus.REQUESTED,
    )
}
