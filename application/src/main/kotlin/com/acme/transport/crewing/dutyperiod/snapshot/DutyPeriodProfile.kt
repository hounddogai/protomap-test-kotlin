package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period profile is in its life cycle. */
enum class DutyPeriodProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One duty period profile, with what the planners need to act on it. */
data class DutyPeriodProfile(
    var code: String,
    var label: String,
    var threshold: Double,
    var country: String,
    var approved: Boolean,
    var odometer: Int,
    var rate: Double,
    var quantity: Int,
    var status: DutyPeriodProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodProfileStatus): Boolean {
        when (status) {
            DutyPeriodProfileStatus.DRAFT -> return next == DutyPeriodProfileStatus.ACTIVE
            DutyPeriodProfileStatus.PENDING -> return next == DutyPeriodProfileStatus.ACTIVE
            DutyPeriodProfileStatus.ACTIVE -> {
                when (next) {
                    DutyPeriodProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            DutyPeriodProfileStatus.SUSPENDED -> return next == DutyPeriodProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DutyPeriodProfileStatus) {
        if (!canMoveTo(next)) {
            error("duty period profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"duty period profile"} $code"

    fun weightedExposure(): Double = round(quantity.toDouble() * threshold * 100) / 100

    fun snapshot(): DutyPeriodProfile {
        return DutyPeriodProfile(
            code = code,
            label = label,
            threshold = threshold,
            country = country,
            approved = approved,
            odometer = odometer,
            rate = rate,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a duty period profile in status draft. */
fun newDutyPeriodProfile(code: String, label: String): DutyPeriodProfile {
    return DutyPeriodProfile(
        code = code,
        label = label,
        threshold = 0.0,
        country = "",
        approved = false,
        odometer = 0,
        rate = 0.0,
        quantity = 0,
        status = DutyPeriodProfileStatus.DRAFT,
    )
}
