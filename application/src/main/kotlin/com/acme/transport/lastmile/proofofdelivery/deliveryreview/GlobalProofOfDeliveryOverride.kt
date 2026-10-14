package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global proof of delivery override is in its life cycle. */
enum class GlobalProofOfDeliveryOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global proof of delivery override as the proof of delivery team keeps it. */
data class GlobalProofOfDeliveryOverride(
    var code: String,
    var name: String,
    var retries: Int,
    var sequence: Int,
    var score: Double,
    var status: GlobalProofOfDeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalProofOfDeliveryOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalProofOfDeliveryOverrideStatus): Boolean {
        when (status) {
            GlobalProofOfDeliveryOverrideStatus.IDLE -> {
                when (next) {
                    GlobalProofOfDeliveryOverrideStatus.READY -> return true
                }
                return false
            }
            GlobalProofOfDeliveryOverrideStatus.WARMING -> return next == GlobalProofOfDeliveryOverrideStatus.READY
            GlobalProofOfDeliveryOverrideStatus.READY -> {
                when (next) {
                    GlobalProofOfDeliveryOverrideStatus.DRAINING -> return true
                }
                return false
            }
            GlobalProofOfDeliveryOverrideStatus.DRAINING -> {
                when (next) {
                    GlobalProofOfDeliveryOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalProofOfDeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global proof of delivery override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((sequence.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalProofOfDeliveryOverride {
        return GlobalProofOfDeliveryOverride(
            code = code,
            name = name,
            retries = retries,
            sequence = sequence,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global proof of delivery override in status idle. */
fun newGlobalProofOfDeliveryOverride(code: String, name: String): GlobalProofOfDeliveryOverride {
    return GlobalProofOfDeliveryOverride(
        code = code,
        name = name,
        retries = 0,
        sequence = 0,
        score = 0.0,
        status = GlobalProofOfDeliveryOverrideStatus.IDLE,
    )
}
