package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery claim records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery claim is in its life cycle. */
enum class ProofOfDeliveryClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a proof of delivery claim and its current status. */
data class ProofOfDeliveryClaim(
    var code: String,
    var name: String,
    var description: String,
    var limit: Int,
    var etaMinutes: Int,
    var rate: Double,
    var windowDays: Int,
    var depot: String,
    var title: String,
    var endsAt: Int,
    var unit: String,
    var status: ProofOfDeliveryClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryClaimStatus): Boolean {
        when (status) {
            ProofOfDeliveryClaimStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliveryClaimStatus.APPROVED, ProofOfDeliveryClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryClaimStatus.APPROVED -> {
                when (next) {
                    ProofOfDeliveryClaimStatus.REJECTED, ProofOfDeliveryClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryClaimStatus.REJECTED -> {
                when (next) {
                    ProofOfDeliveryClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryClaimStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((endsAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryClaim {
        return ProofOfDeliveryClaim(
            code = code,
            name = name,
            description = description,
            limit = limit,
            etaMinutes = etaMinutes,
            rate = rate,
            windowDays = windowDays,
            depot = depot,
            title = title,
            endsAt = endsAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a proof of delivery claim in status proposed. */
fun newProofOfDeliveryClaim(code: String, name: String): ProofOfDeliveryClaim {
    return ProofOfDeliveryClaim(
        code = code,
        name = name,
        description = "",
        limit = 0,
        etaMinutes = 0,
        rate = 0.0,
        windowDays = 0,
        depot = "",
        title = "",
        endsAt = 0,
        unit = "",
        status = ProofOfDeliveryClaimStatus.PROPOSED,
    )
}
