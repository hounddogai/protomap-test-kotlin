package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient locker override is in its life cycle. */
enum class LenientLockerOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient locker override and its current status. */
data class LenientLockerOverride(
    var code: String,
    var title: String,
    var startsAt: Int,
    var unit: String,
    var attempts: Int,
    var status: LenientLockerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLockerOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLockerOverrideStatus): Boolean {
        when (status) {
            LenientLockerOverrideStatus.PLANNED -> return next == LenientLockerOverrideStatus.SCHEDULED
            LenientLockerOverrideStatus.SCHEDULED -> return next == LenientLockerOverrideStatus.IN_PROGRESS
            LenientLockerOverrideStatus.IN_PROGRESS -> return next == LenientLockerOverrideStatus.COMPLETED
            LenientLockerOverrideStatus.COMPLETED -> {
                when (next) {
                    LenientLockerOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLockerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("lenient locker override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient locker override"} $code"

    fun weightedTotal(): Double = round((attempts.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientLockerOverride {
        return LenientLockerOverride(
            code = code,
            title = title,
            startsAt = startsAt,
            unit = unit,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient locker override in status planned. */
fun newLenientLockerOverride(code: String, title: String): LenientLockerOverride {
    return LenientLockerOverride(
        code = code,
        title = title,
        startsAt = 0,
        unit = "",
        attempts = 0,
        status = LenientLockerOverrideStatus.PLANNED,
    )
}
