package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback rest break assignment is in its life cycle. */
enum class FallbackRestBreakAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback rest break assignment and its current status. */
data class FallbackRestBreakAssignment(
    var code: String,
    var name: String,
    var minimum: Double,
    var revision: Int,
    var rate: Double,
    var reviewer: String,
    var fuelLiters: Double,
    var status: FallbackRestBreakAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRestBreakAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRestBreakAssignmentStatus): Boolean {
        when (status) {
            FallbackRestBreakAssignmentStatus.PROPOSED -> return next == FallbackRestBreakAssignmentStatus.APPROVED || next == FallbackRestBreakAssignmentStatus.WITHDRAWN
            FallbackRestBreakAssignmentStatus.APPROVED -> return next == FallbackRestBreakAssignmentStatus.REJECTED || next == FallbackRestBreakAssignmentStatus.WITHDRAWN
            FallbackRestBreakAssignmentStatus.REJECTED -> return next == FallbackRestBreakAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackRestBreakAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback rest break assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((minimum - fuelLiters) * 100) / 100

    fun snapshot(): FallbackRestBreakAssignment {
        return FallbackRestBreakAssignment(
            code = code,
            name = name,
            minimum = minimum,
            revision = revision,
            rate = rate,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback rest break assignment in status proposed. */
fun newFallbackRestBreakAssignment(code: String, name: String): FallbackRestBreakAssignment {
    return FallbackRestBreakAssignment(
        code = code,
        name = name,
        minimum = 0.0,
        revision = 0,
        rate = 0.0,
        reviewer = "",
        fuelLiters = 0.0,
        status = FallbackRestBreakAssignmentStatus.PROPOSED,
    )
}
