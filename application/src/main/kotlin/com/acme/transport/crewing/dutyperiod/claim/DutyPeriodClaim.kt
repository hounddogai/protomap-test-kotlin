package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.normalize

// The duty period claim records of crewing's duty period work, and the rules, stores, and calculations around
// them.

/** Where a duty period claim is in its life cycle. */
enum class DutyPeriodClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One duty period claim, with what the planners need to act on it. */
data class DutyPeriodClaim(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var depot: String,
    var currency: String,
    var status: DutyPeriodClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodClaimStatus): Boolean {
        when (status) {
            DutyPeriodClaimStatus.PROPOSED -> return next == DutyPeriodClaimStatus.APPROVED || next == DutyPeriodClaimStatus.REJECTED
            DutyPeriodClaimStatus.APPROVED -> {
                when (next) {
                    DutyPeriodClaimStatus.REJECTED, DutyPeriodClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodClaimStatus.REJECTED -> {
                when (next) {
                    DutyPeriodClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodClaimStatus) {
        if (!canMoveTo(next)) {
            error("duty period claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): DutyPeriodClaim {
        return DutyPeriodClaim(
            code = code,
            title = title,
            expiresAt = expiresAt,
            depot = depot,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a duty period claim in status proposed. */
fun newDutyPeriodClaim(code: String, title: String): DutyPeriodClaim {
    return DutyPeriodClaim(
        code = code,
        title = title,
        expiresAt = 0,
        depot = "",
        currency = "",
        status = DutyPeriodClaimStatus.PROPOSED,
    )
}
