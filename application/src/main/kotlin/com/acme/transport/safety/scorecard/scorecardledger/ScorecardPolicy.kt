package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard policy is in its life cycle. */
enum class ScorecardPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scorecard policy. Its code is unique within a tenant. */
data class ScorecardPolicy(
    var code: String,
    var name: String,
    var margin: Double,
    var etaMinutes: Int,
    var depot: String,
    var status: ScorecardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardPolicyStatus): Boolean {
        when (status) {
            ScorecardPolicyStatus.REQUESTED -> return next == ScorecardPolicyStatus.GRANTED || next == ScorecardPolicyStatus.EXPIRED
            ScorecardPolicyStatus.GRANTED -> return next == ScorecardPolicyStatus.REVOKED || next == ScorecardPolicyStatus.EXPIRED
            ScorecardPolicyStatus.REVOKED -> {
                when (next) {
                    ScorecardPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scorecard policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() - margin) * 100) / 100

    fun snapshot(): ScorecardPolicy {
        return ScorecardPolicy(
            code = code,
            name = name,
            margin = margin,
            etaMinutes = etaMinutes,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scorecard policy in status requested. */
fun newScorecardPolicy(code: String, name: String): ScorecardPolicy {
    return ScorecardPolicy(
        code = code,
        name = name,
        margin = 0.0,
        etaMinutes = 0,
        depot = "",
        status = ScorecardPolicyStatus.REQUESTED,
    )
}
