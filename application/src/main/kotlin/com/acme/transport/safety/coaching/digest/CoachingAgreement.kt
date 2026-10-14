package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching agreement is in its life cycle. */
enum class CoachingAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a coaching agreement and its current status. */
data class CoachingAgreement(
    var code: String,
    var name: String,
    var vehicleId: String,
    var endsAt: Int,
    var threshold: Double,
    var status: CoachingAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingAgreementStatus): Boolean {
        when (status) {
            CoachingAgreementStatus.DRAFT -> return next == CoachingAgreementStatus.SUSPENDED || next == CoachingAgreementStatus.CLOSED
            CoachingAgreementStatus.PENDING -> return next == CoachingAgreementStatus.CLOSED
            CoachingAgreementStatus.ACTIVE -> {
                when (next) {
                    CoachingAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            CoachingAgreementStatus.SUSPENDED -> {
                when (next) {
                    CoachingAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingAgreementStatus) {
        if (!canMoveTo(next)) {
            error("coaching agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((endsAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): CoachingAgreement {
        return CoachingAgreement(
            code = code,
            name = name,
            vehicleId = vehicleId,
            endsAt = endsAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching agreement in status draft. */
fun newCoachingAgreement(code: String, name: String): CoachingAgreement {
    return CoachingAgreement(
        code = code,
        name = name,
        vehicleId = "",
        endsAt = 0,
        threshold = 0.0,
        status = CoachingAgreementStatus.DRAFT,
    )
}
