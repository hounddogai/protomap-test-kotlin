package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize

// The roster claim records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster claim is in its life cycle. */
enum class RosterClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A roster claim. Its code is unique within a tenant. */
data class RosterClaim(
    var code: String,
    var name: String,
    var currency: String,
    var vehicleId: String,
    var externalRef: String,
    var amount: Double,
    var status: RosterClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterClaimStatus): Boolean {
        when (status) {
            RosterClaimStatus.OPEN -> {
                when (next) {
                    RosterClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            RosterClaimStatus.IN_REVIEW -> return next == RosterClaimStatus.RESOLVED || next == RosterClaimStatus.REOPENED
            RosterClaimStatus.RESOLVED -> return next == RosterClaimStatus.REOPENED
            RosterClaimStatus.REOPENED -> return next == RosterClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RosterClaimStatus) {
        if (!canMoveTo(next)) {
            error("roster claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = amount

    fun snapshot(): RosterClaim {
        return RosterClaim(
            code = code,
            name = name,
            currency = currency,
            vehicleId = vehicleId,
            externalRef = externalRef,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster claim in status open. */
fun newRosterClaim(code: String, name: String): RosterClaim {
    return RosterClaim(
        code = code,
        name = name,
        currency = "",
        vehicleId = "",
        externalRef = "",
        amount = 0.0,
        status = RosterClaimStatus.OPEN,
    )
}
