package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy idling slot is in its life cycle. */
enum class LegacyIdlingSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy idling slot and its current status. */
data class LegacyIdlingSlot(
    var code: String,
    var title: String,
    var budget: Double,
    var graceDays: Int,
    var threshold: Double,
    var segment: String,
    var timezone: String,
    var status: LegacyIdlingSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIdlingSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIdlingSlotStatus): Boolean {
        when (status) {
            LegacyIdlingSlotStatus.PROPOSED -> return next == LegacyIdlingSlotStatus.REJECTED
            LegacyIdlingSlotStatus.APPROVED -> {
                when (next) {
                    LegacyIdlingSlotStatus.REJECTED, LegacyIdlingSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyIdlingSlotStatus.REJECTED -> return next == LegacyIdlingSlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyIdlingSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy idling slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy idling slot"} $code"

    fun weightedValue(): Double = round(budget * graceDays.toDouble() * 100) / 100

    fun snapshot(): LegacyIdlingSlot {
        return LegacyIdlingSlot(
            code = code,
            title = title,
            budget = budget,
            graceDays = graceDays,
            threshold = threshold,
            segment = segment,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy idling slot in status proposed. */
fun newLegacyIdlingSlot(code: String, title: String): LegacyIdlingSlot {
    return LegacyIdlingSlot(
        code = code,
        title = title,
        budget = 0.0,
        graceDays = 0,
        threshold = 0.0,
        segment = "",
        timezone = "",
        status = LegacyIdlingSlotStatus.PROPOSED,
    )
}
