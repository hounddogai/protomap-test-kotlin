package com.acme.transport.crewing.roster.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted roster slot records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a weighted roster slot is in its life cycle. */
enum class WeightedRosterSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One weighted roster slot, with what the planners need to act on it. */
data class WeightedRosterSlot(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var currency: String,
    var tags: MutableList<String>,
    var startsAt: Int,
    var budget: Double,
    var name: String,
    var contactEmail: String,
    var batchSize: Int,
    var status: WeightedRosterSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRosterSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRosterSlotStatus): Boolean {
        when (status) {
            WeightedRosterSlotStatus.PLANNED -> {
                when (next) {
                    WeightedRosterSlotStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedRosterSlotStatus.SCHEDULED -> return next == WeightedRosterSlotStatus.COMPLETED || next == WeightedRosterSlotStatus.CANCELLED
            WeightedRosterSlotStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedRosterSlotStatus.COMPLETED, WeightedRosterSlotStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedRosterSlotStatus.COMPLETED -> {
                when (next) {
                    WeightedRosterSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRosterSlotStatus) {
        if (!canMoveTo(next)) {
            error("weighted roster slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((startsAt.toDouble() - budget) * 100) / 100

    fun snapshot(): WeightedRosterSlot {
        return WeightedRosterSlot(
            code = code,
            title = title,
            distanceKm = distanceKm,
            currency = currency,
            tags = tags,
            startsAt = startsAt,
            budget = budget,
            name = name,
            contactEmail = contactEmail,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted roster slot in status planned. */
fun newWeightedRosterSlot(code: String, title: String): WeightedRosterSlot {
    return WeightedRosterSlot(
        code = code,
        title = title,
        distanceKm = 0.0,
        currency = "",
        tags = mutableListOf<String>(),
        startsAt = 0,
        budget = 0.0,
        name = "",
        contactEmail = "",
        batchSize = 0,
        status = WeightedRosterSlotStatus.PLANNED,
    )
}
