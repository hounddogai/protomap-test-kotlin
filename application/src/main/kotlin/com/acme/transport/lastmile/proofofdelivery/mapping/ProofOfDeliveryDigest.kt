package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery digest is in its life cycle. */
enum class ProofOfDeliveryDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One proof of delivery digest, with what the planners need to act on it. */
data class ProofOfDeliveryDigest(
    var code: String,
    var label: String,
    var quantity: Int,
    var approved: Boolean,
    var distanceKm: Double,
    var category: String,
    var segment: String,
    var status: ProofOfDeliveryDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryDigestStatus): Boolean {
        when (status) {
            ProofOfDeliveryDigestStatus.IDLE -> {
                when (next) {
                    ProofOfDeliveryDigestStatus.DRAINING -> return true
                }
                return false
            }
            ProofOfDeliveryDigestStatus.WARMING -> return next == ProofOfDeliveryDigestStatus.READY || next == ProofOfDeliveryDigestStatus.DRAINING
            ProofOfDeliveryDigestStatus.READY -> {
                when (next) {
                    ProofOfDeliveryDigestStatus.DRAINING -> return true
                }
                return false
            }
            ProofOfDeliveryDigestStatus.DRAINING -> return next == ProofOfDeliveryDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryDigestStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round(quantity.toDouble() * distanceKm * 100) / 100

    fun snapshot(): ProofOfDeliveryDigest {
        return ProofOfDeliveryDigest(
            code = code,
            label = label,
            quantity = quantity,
            approved = approved,
            distanceKm = distanceKm,
            category = category,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a proof of delivery digest in status idle. */
fun newProofOfDeliveryDigest(code: String, label: String): ProofOfDeliveryDigest {
    return ProofOfDeliveryDigest(
        code = code,
        label = label,
        quantity = 0,
        approved = false,
        distanceKm = 0.0,
        category = "",
        segment = "",
        status = ProofOfDeliveryDigestStatus.IDLE,
    )
}
