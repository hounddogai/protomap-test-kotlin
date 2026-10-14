package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period reservation is in its life cycle. */
enum class DutyPeriodReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A duty period reservation. Its code is unique within a tenant. */
data class DutyPeriodReservation(
    var code: String,
    var title: String,
    var weight: Double,
    var country: String,
    var discount: Double,
    var retries: Int,
    var timezone: String,
    var score: Double,
    var status: DutyPeriodReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodReservationStatus): Boolean {
        when (status) {
            DutyPeriodReservationStatus.PROPOSED -> {
                when (next) {
                    DutyPeriodReservationStatus.REJECTED -> return true
                }
                return false
            }
            DutyPeriodReservationStatus.APPROVED -> return next == DutyPeriodReservationStatus.REJECTED || next == DutyPeriodReservationStatus.WITHDRAWN
            DutyPeriodReservationStatus.REJECTED -> return next == DutyPeriodReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DutyPeriodReservationStatus) {
        if (!canMoveTo(next)) {
            error("duty period reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round(score * weight * 100) / 100

    fun snapshot(): DutyPeriodReservation {
        return DutyPeriodReservation(
            code = code,
            title = title,
            weight = weight,
            country = country,
            discount = discount,
            retries = retries,
            timezone = timezone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a duty period reservation in status proposed. */
fun newDutyPeriodReservation(code: String, title: String): DutyPeriodReservation {
    return DutyPeriodReservation(
        code = code,
        title = title,
        weight = 0.0,
        country = "",
        discount = 0.0,
        retries = 0,
        timezone = "",
        score = 0.0,
        status = DutyPeriodReservationStatus.PROPOSED,
    )
}
