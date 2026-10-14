package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.text.normalize

/** Where a proof of delivery policy is in its life cycle. */
enum class ProofOfDeliveryPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A proof of delivery policy as the proof of delivery team keeps it. */
data class ProofOfDeliveryPolicy(
    var code: String,
    var name: String,
    var visible: Boolean,
    var fuelLiters: Double,
    var checksum: String,
    var status: ProofOfDeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryPolicyStatus): Boolean {
        when (status) {
            ProofOfDeliveryPolicyStatus.REQUESTED -> return next == ProofOfDeliveryPolicyStatus.GRANTED || next == ProofOfDeliveryPolicyStatus.EXPIRED
            ProofOfDeliveryPolicyStatus.GRANTED -> return next == ProofOfDeliveryPolicyStatus.REVOKED
            ProofOfDeliveryPolicyStatus.REVOKED -> return next == ProofOfDeliveryPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"proof of delivery policy"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): ProofOfDeliveryPolicy {
        return ProofOfDeliveryPolicy(
            code = code,
            name = name,
            visible = visible,
            fuelLiters = fuelLiters,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery policy in status requested. */
fun newProofOfDeliveryPolicy(code: String, name: String): ProofOfDeliveryPolicy {
    return ProofOfDeliveryPolicy(
        code = code,
        name = name,
        visible = false,
        fuelLiters = 0.0,
        checksum = "",
        status = ProofOfDeliveryPolicyStatus.REQUESTED,
    )
}
