package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.text.normalize

// The proof of delivery review records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery review is in its life cycle. */
enum class ProofOfDeliveryReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery review. Its code is unique within a tenant. */
data class ProofOfDeliveryReview(
    var code: String,
    var name: String,
    var currency: String,
    var driverId: String,
    var retries: Int,
    var trailerId: String,
    var status: ProofOfDeliveryReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryReviewStatus): Boolean {
        when (status) {
            ProofOfDeliveryReviewStatus.PROPOSED -> return next == ProofOfDeliveryReviewStatus.APPROVED
            ProofOfDeliveryReviewStatus.APPROVED -> return next == ProofOfDeliveryReviewStatus.WITHDRAWN
            ProofOfDeliveryReviewStatus.REJECTED -> {
                when (next) {
                    ProofOfDeliveryReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryReviewStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): ProofOfDeliveryReview {
        return ProofOfDeliveryReview(
            code = code,
            name = name,
            currency = currency,
            driverId = driverId,
            retries = retries,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery review in status proposed. */
fun newProofOfDeliveryReview(code: String, name: String): ProofOfDeliveryReview {
    return ProofOfDeliveryReview(
        code = code,
        name = name,
        currency = "",
        driverId = "",
        retries = 0,
        trailerId = "",
        status = ProofOfDeliveryReviewStatus.PROPOSED,
    )
}
