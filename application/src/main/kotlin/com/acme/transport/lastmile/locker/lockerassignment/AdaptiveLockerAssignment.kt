package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive locker assignment records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a adaptive locker assignment is in its life cycle. */
enum class AdaptiveLockerAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a adaptive locker assignment and its current status. */
data class AdaptiveLockerAssignment(
    var code: String,
    var name: String,
    var currency: String,
    var tags: MutableList<String>,
    var channel: String,
    var country: String,
    var depot: String,
    var minimum: Double,
    var amount: Double,
    var lane: String,
    var status: AdaptiveLockerAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLockerAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLockerAssignmentStatus): Boolean {
        when (status) {
            AdaptiveLockerAssignmentStatus.DRAFT -> return next == AdaptiveLockerAssignmentStatus.CLOSED
            AdaptiveLockerAssignmentStatus.PENDING -> return next == AdaptiveLockerAssignmentStatus.ACTIVE || next == AdaptiveLockerAssignmentStatus.CLOSED
            AdaptiveLockerAssignmentStatus.ACTIVE -> return next == AdaptiveLockerAssignmentStatus.SUSPENDED || next == AdaptiveLockerAssignmentStatus.CLOSED
            AdaptiveLockerAssignmentStatus.SUSPENDED -> return next == AdaptiveLockerAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveLockerAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive locker assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive locker assignment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((minimum + amount) * 100) / 100

    fun snapshot(): AdaptiveLockerAssignment {
        return AdaptiveLockerAssignment(
            code = code,
            name = name,
            currency = currency,
            tags = tags,
            channel = channel,
            country = country,
            depot = depot,
            minimum = minimum,
            amount = amount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive locker assignment in status draft. */
fun newAdaptiveLockerAssignment(code: String, name: String): AdaptiveLockerAssignment {
    return AdaptiveLockerAssignment(
        code = code,
        name = name,
        currency = "",
        tags = mutableListOf<String>(),
        channel = "",
        country = "",
        depot = "",
        minimum = 0.0,
        amount = 0.0,
        lane = "",
        status = AdaptiveLockerAssignmentStatus.DRAFT,
    )
}
