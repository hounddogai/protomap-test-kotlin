package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient redelivery slot records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a lenient redelivery slot is in its life cycle. */
enum class LenientRedeliverySlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient redelivery slot as the redelivery team keeps it. */
data class LenientRedeliverySlot(
    var code: String,
    var name: String,
    var sequence: Int,
    var weight: Double,
    var priority: Int,
    var approved: Boolean,
    var routeCode: String,
    var label: String,
    var rate: Double,
    var channel: String,
    var depot: String,
    var status: LenientRedeliverySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRedeliverySlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRedeliverySlotStatus): Boolean {
        when (status) {
            LenientRedeliverySlotStatus.PLANNED -> {
                when (next) {
                    LenientRedeliverySlotStatus.IN_PROGRESS, LenientRedeliverySlotStatus.CANCELLED -> return true
                }
                return false
            }
            LenientRedeliverySlotStatus.SCHEDULED -> {
                when (next) {
                    LenientRedeliverySlotStatus.COMPLETED -> return true
                }
                return false
            }
            LenientRedeliverySlotStatus.IN_PROGRESS -> return next == LenientRedeliverySlotStatus.COMPLETED || next == LenientRedeliverySlotStatus.CANCELLED
            LenientRedeliverySlotStatus.COMPLETED -> return next == LenientRedeliverySlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientRedeliverySlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient redelivery slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedVolume(): Double = round(priority.toDouble() * rate * 100) / 100

    fun snapshot(): LenientRedeliverySlot {
        return LenientRedeliverySlot(
            code = code,
            name = name,
            sequence = sequence,
            weight = weight,
            priority = priority,
            approved = approved,
            routeCode = routeCode,
            label = label,
            rate = rate,
            channel = channel,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient redelivery slot in status planned. */
fun newLenientRedeliverySlot(code: String, name: String): LenientRedeliverySlot {
    return LenientRedeliverySlot(
        code = code,
        name = name,
        sequence = 0,
        weight = 0.0,
        priority = 0,
        approved = false,
        routeCode = "",
        label = "",
        rate = 0.0,
        channel = "",
        depot = "",
        status = LenientRedeliverySlotStatus.PLANNED,
    )
}
