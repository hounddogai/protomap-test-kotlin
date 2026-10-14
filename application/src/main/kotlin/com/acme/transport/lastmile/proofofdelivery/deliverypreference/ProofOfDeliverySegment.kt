package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize

/** Where a proof of delivery segment is in its life cycle. */
enum class ProofOfDeliverySegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery segment as the proof of delivery team keeps it. */
data class ProofOfDeliverySegment(
    var code: String,
    var name: String,
    var channel: String,
    var notes: String,
    var ownerId: String,
    var amount: Double,
    var status: ProofOfDeliverySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliverySegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliverySegmentStatus): Boolean {
        when (status) {
            ProofOfDeliverySegmentStatus.PROPOSED -> return next == ProofOfDeliverySegmentStatus.WITHDRAWN
            ProofOfDeliverySegmentStatus.APPROVED -> return next == ProofOfDeliverySegmentStatus.WITHDRAWN
            ProofOfDeliverySegmentStatus.REJECTED -> {
                when (next) {
                    ProofOfDeliverySegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliverySegmentStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"proof of delivery segment"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): ProofOfDeliverySegment {
        return ProofOfDeliverySegment(
            code = code,
            name = name,
            channel = channel,
            notes = notes,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a proof of delivery segment in status proposed. */
fun newProofOfDeliverySegment(code: String, name: String): ProofOfDeliverySegment {
    return ProofOfDeliverySegment(
        code = code,
        name = name,
        channel = "",
        notes = "",
        ownerId = "",
        amount = 0.0,
        status = ProofOfDeliverySegmentStatus.PROPOSED,
    )
}
