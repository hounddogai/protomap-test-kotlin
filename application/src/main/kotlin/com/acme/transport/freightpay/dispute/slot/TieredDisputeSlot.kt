package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered dispute slot records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a tiered dispute slot is in its life cycle. */
enum class TieredDisputeSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered dispute slot, with what the planners need to act on it. */
data class TieredDisputeSlot(
    var code: String,
    var label: String,
    var active: Boolean,
    var vehicleId: String,
    var archived: Boolean,
    var timezone: String,
    var minimum: Double,
    var trailerId: String,
    var cost: Double,
    var status: TieredDisputeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDisputeSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDisputeSlotStatus): Boolean {
        when (status) {
            TieredDisputeSlotStatus.CREATED -> return next == TieredDisputeSlotStatus.VALIDATED
            TieredDisputeSlotStatus.VALIDATED -> {
                when (next) {
                    TieredDisputeSlotStatus.RETIRED -> return true
                }
                return false
            }
            TieredDisputeSlotStatus.PUBLISHED -> {
                when (next) {
                    TieredDisputeSlotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDisputeSlotStatus) {
        if (!canMoveTo(next)) {
            error("tiered dispute slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round(minimum * cost * 100) / 100

    fun snapshot(): TieredDisputeSlot {
        return TieredDisputeSlot(
            code = code,
            label = label,
            active = active,
            vehicleId = vehicleId,
            archived = archived,
            timezone = timezone,
            minimum = minimum,
            trailerId = trailerId,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered dispute slot in status created. */
fun newTieredDisputeSlot(code: String, label: String): TieredDisputeSlot {
    return TieredDisputeSlot(
        code = code,
        label = label,
        active = false,
        vehicleId = "",
        archived = false,
        timezone = "",
        minimum = 0.0,
        trailerId = "",
        cost = 0.0,
        status = TieredDisputeSlotStatus.CREATED,
    )
}
