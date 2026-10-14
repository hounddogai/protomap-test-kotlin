package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.text.normalize

/** Where a proof of delivery threshold is in its life cycle. */
enum class ProofOfDeliveryThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One proof of delivery threshold, with what the planners need to act on it. */
data class ProofOfDeliveryThreshold(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var pallets: Int,
    var vehicleId: String,
    var status: ProofOfDeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryThresholdStatus): Boolean {
        when (status) {
            ProofOfDeliveryThresholdStatus.REQUESTED -> return next == ProofOfDeliveryThresholdStatus.EXPIRED
            ProofOfDeliveryThresholdStatus.GRANTED -> return next == ProofOfDeliveryThresholdStatus.REVOKED || next == ProofOfDeliveryThresholdStatus.EXPIRED
            ProofOfDeliveryThresholdStatus.REVOKED -> return next == ProofOfDeliveryThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"proof of delivery threshold"} $code"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): ProofOfDeliveryThreshold {
        return ProofOfDeliveryThreshold(
            code = code,
            title = title,
            pinned = pinned,
            pallets = pallets,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery threshold in status requested. */
fun newProofOfDeliveryThreshold(code: String, title: String): ProofOfDeliveryThreshold {
    return ProofOfDeliveryThreshold(
        code = code,
        title = title,
        pinned = false,
        pallets = 0,
        vehicleId = "",
        status = ProofOfDeliveryThresholdStatus.REQUESTED,
    )
}
