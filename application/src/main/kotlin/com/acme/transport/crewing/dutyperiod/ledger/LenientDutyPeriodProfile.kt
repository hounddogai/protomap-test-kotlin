package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.kit.text.normalize

/** Where a lenient duty period profile is in its life cycle. */
enum class LenientDutyPeriodProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient duty period profile as the duty period team keeps it. */
data class LenientDutyPeriodProfile(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var timezone: String,
    var durationMinutes: Int,
    var status: LenientDutyPeriodProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDutyPeriodProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDutyPeriodProfileStatus): Boolean {
        when (status) {
            LenientDutyPeriodProfileStatus.DRAFT -> return next == LenientDutyPeriodProfileStatus.ACTIVE
            LenientDutyPeriodProfileStatus.PENDING -> return next == LenientDutyPeriodProfileStatus.SUSPENDED
            LenientDutyPeriodProfileStatus.ACTIVE -> {
                when (next) {
                    LenientDutyPeriodProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientDutyPeriodProfileStatus.SUSPENDED -> return next == LenientDutyPeriodProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientDutyPeriodProfileStatus) {
        if (!canMoveTo(next)) {
            error("lenient duty period profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): LenientDutyPeriodProfile {
        return LenientDutyPeriodProfile(
            code = code,
            name = name,
            pinned = pinned,
            timezone = timezone,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient duty period profile in status draft. */
fun newLenientDutyPeriodProfile(code: String, name: String): LenientDutyPeriodProfile {
    return LenientDutyPeriodProfile(
        code = code,
        name = name,
        pinned = false,
        timezone = "",
        durationMinutes = 0,
        status = LenientDutyPeriodProfileStatus.DRAFT,
    )
}
