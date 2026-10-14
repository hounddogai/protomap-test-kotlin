package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer assignment is in its life cycle. */
enum class TrailerAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer assignment. Its code is unique within a tenant. */
data class TrailerAssignment(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var fuelLiters: Double,
    var checksum: String,
    var createdAt: Int,
    var startsAt: Int,
    var timezone: String,
    var unit: String,
    var depot: String,
    var budget: Double,
    var status: TrailerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerAssignmentStatus): Boolean {
        when (status) {
            TrailerAssignmentStatus.PROPOSED -> return next == TrailerAssignmentStatus.REJECTED
            TrailerAssignmentStatus.APPROVED -> return next == TrailerAssignmentStatus.WITHDRAWN
            TrailerAssignmentStatus.REJECTED -> return next == TrailerAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TrailerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("trailer assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((startsAt.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): TrailerAssignment {
        return TrailerAssignment(
            code = code,
            name = name,
            pinned = pinned,
            fuelLiters = fuelLiters,
            checksum = checksum,
            createdAt = createdAt,
            startsAt = startsAt,
            timezone = timezone,
            unit = unit,
            depot = depot,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a trailer assignment in status proposed. */
fun newTrailerAssignment(code: String, name: String): TrailerAssignment {
    return TrailerAssignment(
        code = code,
        name = name,
        pinned = false,
        fuelLiters = 0.0,
        checksum = "",
        createdAt = 0,
        startsAt = 0,
        timezone = "",
        unit = "",
        depot = "",
        budget = 0.0,
        status = TrailerAssignmentStatus.PROPOSED,
    )
}
