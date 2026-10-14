package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled proof of delivery policy records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a scheduled proof of delivery policy is in its life cycle. */
enum class ScheduledProofOfDeliveryPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled proof of delivery policy as the proof of delivery team keeps it. */
data class ScheduledProofOfDeliveryPolicy(
    var code: String,
    var name: String,
    var vehicleId: String,
    var externalRef: String,
    var active: Boolean,
    var approved: Boolean,
    var attempts: Int,
    var category: String,
    var rate: Double,
    var status: ScheduledProofOfDeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledProofOfDeliveryPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledProofOfDeliveryPolicyStatus): Boolean {
        when (status) {
            ScheduledProofOfDeliveryPolicyStatus.REQUESTED -> return next == ScheduledProofOfDeliveryPolicyStatus.REVOKED || next == ScheduledProofOfDeliveryPolicyStatus.EXPIRED
            ScheduledProofOfDeliveryPolicyStatus.GRANTED -> {
                when (next) {
                    ScheduledProofOfDeliveryPolicyStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledProofOfDeliveryPolicyStatus.REVOKED -> return next == ScheduledProofOfDeliveryPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledProofOfDeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scheduled proof of delivery policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round((rate + attempts.toDouble()) * 100) / 100

    fun snapshot(): ScheduledProofOfDeliveryPolicy {
        return ScheduledProofOfDeliveryPolicy(
            code = code,
            name = name,
            vehicleId = vehicleId,
            externalRef = externalRef,
            active = active,
            approved = approved,
            attempts = attempts,
            category = category,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled proof of delivery policy in status requested. */
fun newScheduledProofOfDeliveryPolicy(code: String, name: String): ScheduledProofOfDeliveryPolicy {
    return ScheduledProofOfDeliveryPolicy(
        code = code,
        name = name,
        vehicleId = "",
        externalRef = "",
        active = false,
        approved = false,
        attempts = 0,
        category = "",
        rate = 0.0,
        status = ScheduledProofOfDeliveryPolicyStatus.REQUESTED,
    )
}
