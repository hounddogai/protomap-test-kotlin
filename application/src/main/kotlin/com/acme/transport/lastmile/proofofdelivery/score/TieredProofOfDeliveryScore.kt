package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.normalize

// The tiered proof of delivery score records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a tiered proof of delivery score is in its life cycle. */
enum class TieredProofOfDeliveryScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered proof of delivery score and its current status. */
data class TieredProofOfDeliveryScore(
    var code: String,
    var name: String,
    var timezone: String,
    var score: Double,
    var visible: Boolean,
    var ownerId: String,
    var lane: String,
    var checksum: String,
    var region: String,
    var carrier: String,
    var locale: String,
    var status: TieredProofOfDeliveryScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredProofOfDeliveryScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredProofOfDeliveryScoreStatus): Boolean {
        when (status) {
            TieredProofOfDeliveryScoreStatus.PROPOSED -> return next == TieredProofOfDeliveryScoreStatus.APPROVED || next == TieredProofOfDeliveryScoreStatus.WITHDRAWN
            TieredProofOfDeliveryScoreStatus.APPROVED -> {
                when (next) {
                    TieredProofOfDeliveryScoreStatus.REJECTED -> return true
                }
                return false
            }
            TieredProofOfDeliveryScoreStatus.REJECTED -> return next == TieredProofOfDeliveryScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredProofOfDeliveryScoreStatus) {
        if (!canMoveTo(next)) {
            error("tiered proof of delivery score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = score

    fun snapshot(): TieredProofOfDeliveryScore {
        return TieredProofOfDeliveryScore(
            code = code,
            name = name,
            timezone = timezone,
            score = score,
            visible = visible,
            ownerId = ownerId,
            lane = lane,
            checksum = checksum,
            region = region,
            carrier = carrier,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered proof of delivery score in status proposed. */
fun newTieredProofOfDeliveryScore(code: String, name: String): TieredProofOfDeliveryScore {
    return TieredProofOfDeliveryScore(
        code = code,
        name = name,
        timezone = "",
        score = 0.0,
        visible = false,
        ownerId = "",
        lane = "",
        checksum = "",
        region = "",
        carrier = "",
        locale = "",
        status = TieredProofOfDeliveryScoreStatus.PROPOSED,
    )
}
