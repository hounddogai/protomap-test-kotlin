package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.normalize

// The proof of delivery estimate records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery estimate is in its life cycle. */
enum class ProofOfDeliveryEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One proof of delivery estimate, with what the planners need to act on it. */
data class ProofOfDeliveryEstimate(
    var code: String,
    var label: String,
    var approved: Boolean,
    var weight: Double,
    var driverId: String,
    var depot: String,
    var status: ProofOfDeliveryEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryEstimateStatus): Boolean {
        when (status) {
            ProofOfDeliveryEstimateStatus.IDLE -> return next == ProofOfDeliveryEstimateStatus.STOPPED
            ProofOfDeliveryEstimateStatus.WARMING -> return next == ProofOfDeliveryEstimateStatus.READY
            ProofOfDeliveryEstimateStatus.READY -> {
                when (next) {
                    ProofOfDeliveryEstimateStatus.DRAINING, ProofOfDeliveryEstimateStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliveryEstimateStatus.DRAINING -> return next == ProofOfDeliveryEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryEstimateStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"proof of delivery estimate"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): ProofOfDeliveryEstimate {
        return ProofOfDeliveryEstimate(
            code = code,
            label = label,
            approved = approved,
            weight = weight,
            driverId = driverId,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a proof of delivery estimate in status idle. */
fun newProofOfDeliveryEstimate(code: String, label: String): ProofOfDeliveryEstimate {
    return ProofOfDeliveryEstimate(
        code = code,
        label = label,
        approved = false,
        weight = 0.0,
        driverId = "",
        depot = "",
        status = ProofOfDeliveryEstimateStatus.IDLE,
    )
}
