package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted availability slot is in its life cycle. */
enum class WeightedAvailabilitySlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One weighted availability slot, with what the planners need to act on it. */
data class WeightedAvailabilitySlot(
    var code: String,
    var name: String,
    var timezone: String,
    var discount: Double,
    var score: Double,
    var carrier: String,
    var title: String,
    var checksum: String,
    var currency: String,
    var lane: String,
    var active: Boolean,
    var status: WeightedAvailabilitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAvailabilitySlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAvailabilitySlotStatus): Boolean {
        when (status) {
            WeightedAvailabilitySlotStatus.PROPOSED -> {
                when (next) {
                    WeightedAvailabilitySlotStatus.REJECTED -> return true
                }
                return false
            }
            WeightedAvailabilitySlotStatus.APPROVED -> {
                when (next) {
                    WeightedAvailabilitySlotStatus.REJECTED -> return true
                }
                return false
            }
            WeightedAvailabilitySlotStatus.REJECTED -> {
                when (next) {
                    WeightedAvailabilitySlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAvailabilitySlotStatus) {
        if (!canMoveTo(next)) {
            error("weighted availability slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted availability slot"} $code"

    fun adjustedExposure(): Double = round(score * discount * 100) / 100

    fun snapshot(): WeightedAvailabilitySlot {
        return WeightedAvailabilitySlot(
            code = code,
            name = name,
            timezone = timezone,
            discount = discount,
            score = score,
            carrier = carrier,
            title = title,
            checksum = checksum,
            currency = currency,
            lane = lane,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted availability slot in status proposed. */
fun newWeightedAvailabilitySlot(code: String, name: String): WeightedAvailabilitySlot {
    return WeightedAvailabilitySlot(
        code = code,
        name = name,
        timezone = "",
        discount = 0.0,
        score = 0.0,
        carrier = "",
        title = "",
        checksum = "",
        currency = "",
        lane = "",
        active = false,
        status = WeightedAvailabilitySlotStatus.PROPOSED,
    )
}
