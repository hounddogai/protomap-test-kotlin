package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss claim records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss claim is in its life cycle. */
enum class NearMissClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A near miss claim. Its code is unique within a tenant. */
data class NearMissClaim(
    var code: String,
    var title: String,
    var lane: String,
    var ceiling: Double,
    var minimum: Double,
    var segment: String,
    var approved: Boolean,
    var carrier: String,
    var reviewer: String,
    var status: NearMissClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissClaimStatus): Boolean {
        when (status) {
            NearMissClaimStatus.PROPOSED -> {
                when (next) {
                    NearMissClaimStatus.APPROVED -> return true
                }
                return false
            }
            NearMissClaimStatus.APPROVED -> return next == NearMissClaimStatus.REJECTED
            NearMissClaimStatus.REJECTED -> {
                when (next) {
                    NearMissClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissClaimStatus) {
        if (!canMoveTo(next)) {
            error("near miss claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss claim"} $code"

    fun adjustedValue(): Double = round((minimum + ceiling) * 100) / 100

    fun snapshot(): NearMissClaim {
        return NearMissClaim(
            code = code,
            title = title,
            lane = lane,
            ceiling = ceiling,
            minimum = minimum,
            segment = segment,
            approved = approved,
            carrier = carrier,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss claim in status proposed. */
fun newNearMissClaim(code: String, title: String): NearMissClaim {
    return NearMissClaim(
        code = code,
        title = title,
        lane = "",
        ceiling = 0.0,
        minimum = 0.0,
        segment = "",
        approved = false,
        carrier = "",
        reviewer = "",
        status = NearMissClaimStatus.PROPOSED,
    )
}
