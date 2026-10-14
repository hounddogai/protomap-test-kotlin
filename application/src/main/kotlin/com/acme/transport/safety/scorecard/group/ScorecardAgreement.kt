package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard agreement is in its life cycle. */
enum class ScorecardAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scorecard agreement, with what the planners need to act on it. */
data class ScorecardAgreement(
    var code: String,
    var name: String,
    var amount: Double,
    var cost: Double,
    var unit: String,
    var status: ScorecardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardAgreementStatus): Boolean {
        when (status) {
            ScorecardAgreementStatus.DRAFT -> return next == ScorecardAgreementStatus.ACTIVE || next == ScorecardAgreementStatus.SUSPENDED
            ScorecardAgreementStatus.PENDING -> return next == ScorecardAgreementStatus.ACTIVE || next == ScorecardAgreementStatus.CLOSED
            ScorecardAgreementStatus.ACTIVE -> {
                when (next) {
                    ScorecardAgreementStatus.SUSPENDED, ScorecardAgreementStatus.CLOSED -> return true
                }
                return false
            }
            ScorecardAgreementStatus.SUSPENDED -> return next == ScorecardAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScorecardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("scorecard agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round(cost * amount * 100) / 100

    fun snapshot(): ScorecardAgreement {
        return ScorecardAgreement(
            code = code,
            name = name,
            amount = amount,
            cost = cost,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scorecard agreement in status draft. */
fun newScorecardAgreement(code: String, name: String): ScorecardAgreement {
    return ScorecardAgreement(
        code = code,
        name = name,
        amount = 0.0,
        cost = 0.0,
        unit = "",
        status = ScorecardAgreementStatus.DRAFT,
    )
}
