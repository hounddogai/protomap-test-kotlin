package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict node tier records of network's node work, and the rules, stores, and calculations around them.

/** Where a strict node tier is in its life cycle. */
enum class StrictNodeTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One strict node tier, with what the planners need to act on it. */
data class StrictNodeTier(
    var code: String,
    var title: String,
    var approved: Boolean,
    var floorValue: Double,
    var maximum: Double,
    var status: StrictNodeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNodeTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNodeTierStatus): Boolean {
        when (status) {
            StrictNodeTierStatus.PLANNED -> return next == StrictNodeTierStatus.COMPLETED
            StrictNodeTierStatus.SCHEDULED -> {
                when (next) {
                    StrictNodeTierStatus.IN_PROGRESS, StrictNodeTierStatus.CANCELLED -> return true
                }
                return false
            }
            StrictNodeTierStatus.IN_PROGRESS -> return next == StrictNodeTierStatus.COMPLETED || next == StrictNodeTierStatus.CANCELLED
            StrictNodeTierStatus.COMPLETED -> return next == StrictNodeTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictNodeTierStatus) {
        if (!canMoveTo(next)) {
            error("strict node tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((maximum + floorValue) * 100) / 100

    fun snapshot(): StrictNodeTier {
        return StrictNodeTier(
            code = code,
            title = title,
            approved = approved,
            floorValue = floorValue,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict node tier in status planned. */
fun newStrictNodeTier(code: String, title: String): StrictNodeTier {
    return StrictNodeTier(
        code = code,
        title = title,
        approved = false,
        floorValue = 0.0,
        maximum = 0.0,
        status = StrictNodeTierStatus.PLANNED,
    )
}
