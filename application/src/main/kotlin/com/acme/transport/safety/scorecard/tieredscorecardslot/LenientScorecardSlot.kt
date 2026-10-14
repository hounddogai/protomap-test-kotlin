package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient scorecard slot records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a lenient scorecard slot is in its life cycle. */
enum class LenientScorecardSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient scorecard slot and its current status. */
data class LenientScorecardSlot(
    var code: String,
    var name: String,
    var country: String,
    var graceDays: Int,
    var unit: String,
    var threshold: Double,
    var retries: Int,
    var durationMinutes: Int,
    var phone: String,
    var title: String,
    var version: Int,
    var status: LenientScorecardSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientScorecardSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientScorecardSlotStatus): Boolean {
        when (status) {
            LenientScorecardSlotStatus.OPEN -> {
                when (next) {
                    LenientScorecardSlotStatus.RESOLVED -> return true
                }
                return false
            }
            LenientScorecardSlotStatus.IN_REVIEW -> {
                when (next) {
                    LenientScorecardSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientScorecardSlotStatus.RESOLVED -> {
                when (next) {
                    LenientScorecardSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientScorecardSlotStatus.REOPENED -> {
                when (next) {
                    LenientScorecardSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientScorecardSlotStatus) {
        if (!canMoveTo(next)) {
            error("lenient scorecard slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"lenient scorecard slot"} $code"

    fun weightedExposure(): Double = round(graceDays.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): LenientScorecardSlot {
        return LenientScorecardSlot(
            code = code,
            name = name,
            country = country,
            graceDays = graceDays,
            unit = unit,
            threshold = threshold,
            retries = retries,
            durationMinutes = durationMinutes,
            phone = phone,
            title = title,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient scorecard slot in status open. */
fun newLenientScorecardSlot(code: String, name: String): LenientScorecardSlot {
    return LenientScorecardSlot(
        code = code,
        name = name,
        country = "",
        graceDays = 0,
        unit = "",
        threshold = 0.0,
        retries = 0,
        durationMinutes = 0,
        phone = "",
        title = "",
        version = 0,
        status = LenientScorecardSlotStatus.OPEN,
    )
}
