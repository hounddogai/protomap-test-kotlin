package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted availability draft is in its life cycle. */
enum class WeightedAvailabilityDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted availability draft as the availability team keeps it. */
data class WeightedAvailabilityDraft(
    var code: String,
    var name: String,
    var currency: String,
    var attempts: Int,
    var fuelLiters: Double,
    var lane: String,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var budget: Double,
    var source: String,
    var status: WeightedAvailabilityDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAvailabilityDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAvailabilityDraftStatus): Boolean {
        when (status) {
            WeightedAvailabilityDraftStatus.CREATED -> return next == WeightedAvailabilityDraftStatus.VALIDATED
            WeightedAvailabilityDraftStatus.VALIDATED -> {
                when (next) {
                    WeightedAvailabilityDraftStatus.RETIRED -> return true
                }
                return false
            }
            WeightedAvailabilityDraftStatus.PUBLISHED -> return next == WeightedAvailabilityDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedAvailabilityDraftStatus) {
        if (!canMoveTo(next)) {
            error("weighted availability draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted availability draft"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(budget * attempts.toDouble() * 100) / 100

    fun snapshot(): WeightedAvailabilityDraft {
        return WeightedAvailabilityDraft(
            code = code,
            name = name,
            currency = currency,
            attempts = attempts,
            fuelLiters = fuelLiters,
            lane = lane,
            pinned = pinned,
            tags = tags,
            budget = budget,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted availability draft in status created. */
fun newWeightedAvailabilityDraft(code: String, name: String): WeightedAvailabilityDraft {
    return WeightedAvailabilityDraft(
        code = code,
        name = name,
        currency = "",
        attempts = 0,
        fuelLiters = 0.0,
        lane = "",
        pinned = false,
        tags = mutableListOf<String>(),
        budget = 0.0,
        source = "",
        status = WeightedAvailabilityDraftStatus.CREATED,
    )
}
