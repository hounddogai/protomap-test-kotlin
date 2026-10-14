package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict coaching agreement is in its life cycle. */
enum class StrictCoachingAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict coaching agreement as the coaching team keeps it. */
data class StrictCoachingAgreement(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var attempts: Int,
    var startsAt: Int,
    var archived: Boolean,
    var depot: String,
    var margin: Double,
    var distanceKm: Double,
    var status: StrictCoachingAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCoachingAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCoachingAgreementStatus): Boolean {
        when (status) {
            StrictCoachingAgreementStatus.DRAFT -> return next == StrictCoachingAgreementStatus.PENDING || next == StrictCoachingAgreementStatus.ACTIVE
            StrictCoachingAgreementStatus.PENDING -> {
                when (next) {
                    StrictCoachingAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictCoachingAgreementStatus.ACTIVE -> return next == StrictCoachingAgreementStatus.SUSPENDED
            StrictCoachingAgreementStatus.SUSPENDED -> return next == StrictCoachingAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictCoachingAgreementStatus) {
        if (!canMoveTo(next)) {
            error("strict coaching agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict coaching agreement"} $code"

    fun effectiveValue(): Double = round((startsAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): StrictCoachingAgreement {
        return StrictCoachingAgreement(
            code = code,
            label = label,
            updatedAt = updatedAt,
            attempts = attempts,
            startsAt = startsAt,
            archived = archived,
            depot = depot,
            margin = margin,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict coaching agreement in status draft. */
fun newStrictCoachingAgreement(code: String, label: String): StrictCoachingAgreement {
    return StrictCoachingAgreement(
        code = code,
        label = label,
        updatedAt = 0,
        attempts = 0,
        startsAt = 0,
        archived = false,
        depot = "",
        margin = 0.0,
        distanceKm = 0.0,
        status = StrictCoachingAgreementStatus.DRAFT,
    )
}
