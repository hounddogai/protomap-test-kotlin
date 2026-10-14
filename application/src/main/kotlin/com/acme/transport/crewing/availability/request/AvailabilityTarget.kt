package com.acme.transport.crewing.availability.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability target is in its life cycle. */
enum class AvailabilityTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A availability target as the availability team keeps it. */
data class AvailabilityTarget(
    var code: String,
    var name: String,
    var discount: Double,
    var attempts: Int,
    var archived: Boolean,
    var status: AvailabilityTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityTargetStatus): Boolean {
        when (status) {
            AvailabilityTargetStatus.OPEN -> {
                when (next) {
                    AvailabilityTargetStatus.RESOLVED, AvailabilityTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityTargetStatus.IN_REVIEW -> {
                when (next) {
                    AvailabilityTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityTargetStatus.RESOLVED -> return next == AvailabilityTargetStatus.REOPENED
            AvailabilityTargetStatus.REOPENED -> {
                when (next) {
                    AvailabilityTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityTargetStatus) {
        if (!canMoveTo(next)) {
            error("availability target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round(attempts.toDouble() * discount * 100) / 100

    fun snapshot(): AvailabilityTarget {
        return AvailabilityTarget(
            code = code,
            name = name,
            discount = discount,
            attempts = attempts,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability target in status open. */
fun newAvailabilityTarget(code: String, name: String): AvailabilityTarget {
    return AvailabilityTarget(
        code = code,
        name = name,
        discount = 0.0,
        attempts = 0,
        archived = false,
        status = AvailabilityTargetStatus.OPEN,
    )
}
