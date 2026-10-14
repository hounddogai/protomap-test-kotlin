package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery profile is in its life cycle. */
enum class ProofOfDeliveryProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery profile as the proof of delivery team keeps it. */
data class ProofOfDeliveryProfile(
    var code: String,
    var title: String,
    var ownerId: String,
    var threshold: Double,
    var quantity: Int,
    var status: ProofOfDeliveryProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryProfileStatus): Boolean {
        when (status) {
            ProofOfDeliveryProfileStatus.PROPOSED -> return next == ProofOfDeliveryProfileStatus.APPROVED
            ProofOfDeliveryProfileStatus.APPROVED -> {
                when (next) {
                    ProofOfDeliveryProfileStatus.REJECTED -> return true
                }
                return false
            }
            ProofOfDeliveryProfileStatus.REJECTED -> return next == ProofOfDeliveryProfileStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryProfileStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round(threshold * quantity.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryProfile {
        return ProofOfDeliveryProfile(
            code = code,
            title = title,
            ownerId = ownerId,
            threshold = threshold,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery profile in status proposed. */
fun newProofOfDeliveryProfile(code: String, title: String): ProofOfDeliveryProfile {
    return ProofOfDeliveryProfile(
        code = code,
        title = title,
        ownerId = "",
        threshold = 0.0,
        quantity = 0,
        status = ProofOfDeliveryProfileStatus.PROPOSED,
    )
}
