package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient cutoff claim records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a lenient cutoff claim is in its life cycle. */
enum class LenientCutoffClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient cutoff claim as the cutoff team keeps it. */
data class LenientCutoffClaim(
    var code: String,
    var name: String,
    var cost: Double,
    var createdAt: Int,
    var title: String,
    var updatedAt: Int,
    var status: LenientCutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCutoffClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCutoffClaimStatus): Boolean {
        when (status) {
            LenientCutoffClaimStatus.PROPOSED -> {
                when (next) {
                    LenientCutoffClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientCutoffClaimStatus.APPROVED -> return next == LenientCutoffClaimStatus.REJECTED || next == LenientCutoffClaimStatus.WITHDRAWN
            LenientCutoffClaimStatus.REJECTED -> return next == LenientCutoffClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientCutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient cutoff claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((updatedAt.toDouble() - cost) * 100) / 100

    fun snapshot(): LenientCutoffClaim {
        return LenientCutoffClaim(
            code = code,
            name = name,
            cost = cost,
            createdAt = createdAt,
            title = title,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient cutoff claim in status proposed. */
fun newLenientCutoffClaim(code: String, name: String): LenientCutoffClaim {
    return LenientCutoffClaim(
        code = code,
        name = name,
        cost = 0.0,
        createdAt = 0,
        title = "",
        updatedAt = 0,
        status = LenientCutoffClaimStatus.PROPOSED,
    )
}
