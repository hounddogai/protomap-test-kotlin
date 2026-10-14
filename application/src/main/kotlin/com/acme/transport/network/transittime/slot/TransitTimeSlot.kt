package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time slot records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time slot is in its life cycle. */
enum class TransitTimeSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A transit time slot as the transit time team keeps it. */
data class TransitTimeSlot(
    var code: String,
    var title: String,
    var priority: Int,
    var notes: String,
    var checksum: String,
    var etaMinutes: Int,
    var amount: Double,
    var status: TransitTimeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeSlotStatus): Boolean {
        when (status) {
            TransitTimeSlotStatus.OPEN -> return next == TransitTimeSlotStatus.IN_REVIEW
            TransitTimeSlotStatus.IN_REVIEW -> return next == TransitTimeSlotStatus.ARCHIVED
            TransitTimeSlotStatus.RESOLVED -> return next == TransitTimeSlotStatus.REOPENED || next == TransitTimeSlotStatus.ARCHIVED
            TransitTimeSlotStatus.REOPENED -> {
                when (next) {
                    TransitTimeSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeSlotStatus) {
        if (!canMoveTo(next)) {
            error("transit time slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(etaMinutes.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): TransitTimeSlot {
        return TransitTimeSlot(
            code = code,
            title = title,
            priority = priority,
            notes = notes,
            checksum = checksum,
            etaMinutes = etaMinutes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time slot in status open. */
fun newTransitTimeSlot(code: String, title: String): TransitTimeSlot {
    return TransitTimeSlot(
        code = code,
        title = title,
        priority = 0,
        notes = "",
        checksum = "",
        etaMinutes = 0,
        amount = 0.0,
        status = TransitTimeSlotStatus.OPEN,
    )
}
