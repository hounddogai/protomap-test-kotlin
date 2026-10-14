package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching reservation is in its life cycle. */
enum class CoachingReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A coaching reservation as the coaching team keeps it. */
data class CoachingReservation(
    var code: String,
    var name: String,
    var amount: Double,
    var approved: Boolean,
    var routeCode: String,
    var rate: Double,
    var discount: Double,
    var active: Boolean,
    var version: Int,
    var title: String,
    var maximum: Double,
    var status: CoachingReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingReservationStatus): Boolean {
        when (status) {
            CoachingReservationStatus.OPEN -> return next == CoachingReservationStatus.IN_REVIEW
            CoachingReservationStatus.IN_REVIEW -> {
                when (next) {
                    CoachingReservationStatus.RESOLVED, CoachingReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingReservationStatus.RESOLVED -> {
                when (next) {
                    CoachingReservationStatus.REOPENED, CoachingReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingReservationStatus.REOPENED -> {
                when (next) {
                    CoachingReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingReservationStatus) {
        if (!canMoveTo(next)) {
            error("coaching reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((amount + maximum) * 100) / 100

    fun snapshot(): CoachingReservation {
        return CoachingReservation(
            code = code,
            name = name,
            amount = amount,
            approved = approved,
            routeCode = routeCode,
            rate = rate,
            discount = discount,
            active = active,
            version = version,
            title = title,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching reservation in status open. */
fun newCoachingReservation(code: String, name: String): CoachingReservation {
    return CoachingReservation(
        code = code,
        name = name,
        amount = 0.0,
        approved = false,
        routeCode = "",
        rate = 0.0,
        discount = 0.0,
        active = false,
        version = 0,
        title = "",
        maximum = 0.0,
        status = CoachingReservationStatus.OPEN,
    )
}
