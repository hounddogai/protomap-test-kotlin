package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual proof of delivery claim records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a manual proof of delivery claim is in its life cycle. */
enum class ManualProofOfDeliveryClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual proof of delivery claim, with what the planners need to act on it. */
data class ManualProofOfDeliveryClaim(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var depot: String,
    var channel: String,
    var retries: Int,
    var status: ManualProofOfDeliveryClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryClaimStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryClaimStatus.PROPOSED -> {
                when (next) {
                    ManualProofOfDeliveryClaimStatus.APPROVED -> return true
                }
                return false
            }
            ManualProofOfDeliveryClaimStatus.APPROVED -> return next == ManualProofOfDeliveryClaimStatus.WITHDRAWN
            ManualProofOfDeliveryClaimStatus.REJECTED -> {
                when (next) {
                    ManualProofOfDeliveryClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual proof of delivery claim"} $code"

    fun projectedValue(): Double = round((retries.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): ManualProofOfDeliveryClaim {
        return ManualProofOfDeliveryClaim(
            code = code,
            label = label,
            distanceKm = distanceKm,
            depot = depot,
            channel = channel,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual proof of delivery claim in status proposed. */
fun newManualProofOfDeliveryClaim(code: String, label: String): ManualProofOfDeliveryClaim {
    return ManualProofOfDeliveryClaim(
        code = code,
        label = label,
        distanceKm = 0.0,
        depot = "",
        channel = "",
        retries = 0,
        status = ManualProofOfDeliveryClaimStatus.PROPOSED,
    )
}
