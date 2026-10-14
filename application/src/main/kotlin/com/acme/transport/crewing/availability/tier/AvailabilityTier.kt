package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability tier records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability tier is in its life cycle. */
enum class AvailabilityTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One availability tier, with what the planners need to act on it. */
data class AvailabilityTier(
    var code: String,
    var label: String,
    var amount: Double,
    var score: Double,
    var floorValue: Double,
    var approved: Boolean,
    var minimum: Double,
    var status: AvailabilityTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityTierStatus): Boolean {
        when (status) {
            AvailabilityTierStatus.OPEN -> return next == AvailabilityTierStatus.REOPENED || next == AvailabilityTierStatus.ARCHIVED
            AvailabilityTierStatus.IN_REVIEW -> {
                when (next) {
                    AvailabilityTierStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityTierStatus.RESOLVED -> return next == AvailabilityTierStatus.REOPENED || next == AvailabilityTierStatus.ARCHIVED
            AvailabilityTierStatus.REOPENED -> {
                when (next) {
                    AvailabilityTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityTierStatus) {
        if (!canMoveTo(next)) {
            error("availability tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"availability tier"} $code"

    fun projectedLoad(): Double = round((score + amount) * 100) / 100

    fun snapshot(): AvailabilityTier {
        return AvailabilityTier(
            code = code,
            label = label,
            amount = amount,
            score = score,
            floorValue = floorValue,
            approved = approved,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability tier in status open. */
fun newAvailabilityTier(code: String, label: String): AvailabilityTier {
    return AvailabilityTier(
        code = code,
        label = label,
        amount = 0.0,
        score = 0.0,
        floorValue = 0.0,
        approved = false,
        minimum = 0.0,
        status = AvailabilityTierStatus.OPEN,
    )
}
