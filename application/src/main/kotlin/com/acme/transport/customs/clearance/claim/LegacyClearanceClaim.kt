package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.text.normalize

// The legacy clearance claim records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a legacy clearance claim is in its life cycle. */
enum class LegacyClearanceClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy clearance claim, with what the planners need to act on it. */
data class LegacyClearanceClaim(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var approved: Boolean,
    var carrier: String,
    var status: LegacyClearanceClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceClaimStatus): Boolean {
        when (status) {
            LegacyClearanceClaimStatus.OPEN -> {
                when (next) {
                    LegacyClearanceClaimStatus.REOPENED -> return true
                }
                return false
            }
            LegacyClearanceClaimStatus.IN_REVIEW -> return next == LegacyClearanceClaimStatus.REOPENED
            LegacyClearanceClaimStatus.RESOLVED -> {
                when (next) {
                    LegacyClearanceClaimStatus.REOPENED -> return true
                }
                return false
            }
            LegacyClearanceClaimStatus.REOPENED -> return next == LegacyClearanceClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyClearanceClaimStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy clearance claim"} $code"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): LegacyClearanceClaim {
        return LegacyClearanceClaim(
            code = code,
            name = name,
            updatedAt = updatedAt,
            approved = approved,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy clearance claim in status open. */
fun newLegacyClearanceClaim(code: String, name: String): LegacyClearanceClaim {
    return LegacyClearanceClaim(
        code = code,
        name = name,
        updatedAt = 0,
        approved = false,
        carrier = "",
        status = LegacyClearanceClaimStatus.OPEN,
    )
}
