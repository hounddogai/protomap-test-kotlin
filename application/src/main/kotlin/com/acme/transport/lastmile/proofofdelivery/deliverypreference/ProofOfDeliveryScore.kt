package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery score is in its life cycle. */
enum class ProofOfDeliveryScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery score as the proof of delivery team keeps it. */
data class ProofOfDeliveryScore(
    var code: String,
    var title: String,
    var ceiling: Double,
    var visible: Boolean,
    var carrier: String,
    var timezone: String,
    var depot: String,
    var weight: Double,
    var sequence: Int,
    var reviewer: String,
    var status: ProofOfDeliveryScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryScoreStatus): Boolean {
        when (status) {
            ProofOfDeliveryScoreStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliveryScoreStatus.APPROVED, ProofOfDeliveryScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryScoreStatus.APPROVED -> return next == ProofOfDeliveryScoreStatus.REJECTED || next == ProofOfDeliveryScoreStatus.WITHDRAWN
            ProofOfDeliveryScoreStatus.REJECTED -> return next == ProofOfDeliveryScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryScoreStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((weight + sequence.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryScore {
        return ProofOfDeliveryScore(
            code = code,
            title = title,
            ceiling = ceiling,
            visible = visible,
            carrier = carrier,
            timezone = timezone,
            depot = depot,
            weight = weight,
            sequence = sequence,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a proof of delivery score in status proposed. */
fun newProofOfDeliveryScore(code: String, title: String): ProofOfDeliveryScore {
    return ProofOfDeliveryScore(
        code = code,
        title = title,
        ceiling = 0.0,
        visible = false,
        carrier = "",
        timezone = "",
        depot = "",
        weight = 0.0,
        sequence = 0,
        reviewer = "",
        status = ProofOfDeliveryScoreStatus.PROPOSED,
    )
}
