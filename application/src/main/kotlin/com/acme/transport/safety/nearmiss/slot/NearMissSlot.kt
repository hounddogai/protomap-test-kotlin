package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss slot records of safety's near miss work, and the rules, stores, and calculations around them.

/** Where a near miss slot is in its life cycle. */
enum class NearMissSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One near miss slot, with what the planners need to act on it. */
data class NearMissSlot(
    var code: String,
    var title: String,
    var contactEmail: String,
    var summary: String,
    var discount: Double,
    var endsAt: Int,
    var channel: String,
    var trailerId: String,
    var driverId: String,
    var status: NearMissSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissSlotStatus): Boolean {
        when (status) {
            NearMissSlotStatus.CREATED -> {
                when (next) {
                    NearMissSlotStatus.VALIDATED, NearMissSlotStatus.PUBLISHED -> return true
                }
                return false
            }
            NearMissSlotStatus.VALIDATED -> return next == NearMissSlotStatus.PUBLISHED || next == NearMissSlotStatus.RETIRED
            NearMissSlotStatus.PUBLISHED -> return next == NearMissSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NearMissSlotStatus) {
        if (!canMoveTo(next)) {
            error("near miss slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"near miss slot"} $code"

    fun weightedLoad(): Double = round((discount + endsAt.toDouble()) * 100) / 100

    fun snapshot(): NearMissSlot {
        return NearMissSlot(
            code = code,
            title = title,
            contactEmail = contactEmail,
            summary = summary,
            discount = discount,
            endsAt = endsAt,
            channel = channel,
            trailerId = trailerId,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a near miss slot in status created. */
fun newNearMissSlot(code: String, title: String): NearMissSlot {
    return NearMissSlot(
        code = code,
        title = title,
        contactEmail = "",
        summary = "",
        discount = 0.0,
        endsAt = 0,
        channel = "",
        trailerId = "",
        driverId = "",
        status = NearMissSlotStatus.CREATED,
    )
}
