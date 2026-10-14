package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery override is in its life cycle. */
enum class ProofOfDeliveryOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A proof of delivery override as the proof of delivery team keeps it. */
data class ProofOfDeliveryOverride(
    var code: String,
    var name: String,
    var approved: Boolean,
    var minimum: Double,
    var revision: Int,
    var status: ProofOfDeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryOverrideStatus): Boolean {
        when (status) {
            ProofOfDeliveryOverrideStatus.IDLE -> {
                when (next) {
                    ProofOfDeliveryOverrideStatus.READY -> return true
                }
                return false
            }
            ProofOfDeliveryOverrideStatus.WARMING -> {
                when (next) {
                    ProofOfDeliveryOverrideStatus.READY, ProofOfDeliveryOverrideStatus.DRAINING -> return true
                }
                return false
            }
            ProofOfDeliveryOverrideStatus.READY -> {
                when (next) {
                    ProofOfDeliveryOverrideStatus.STOPPED -> return true
                }
                return false
            }
            ProofOfDeliveryOverrideStatus.DRAINING -> {
                when (next) {
                    ProofOfDeliveryOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery override $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"proof of delivery override"} $code"

    fun weightedExposure(): Double = round((revision.toDouble() - minimum) * 100) / 100

    fun snapshot(): ProofOfDeliveryOverride {
        return ProofOfDeliveryOverride(
            code = code,
            name = name,
            approved = approved,
            minimum = minimum,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery override in status idle. */
fun newProofOfDeliveryOverride(code: String, name: String): ProofOfDeliveryOverride {
    return ProofOfDeliveryOverride(
        code = code,
        name = name,
        approved = false,
        minimum = 0.0,
        revision = 0,
        status = ProofOfDeliveryOverrideStatus.IDLE,
    )
}
