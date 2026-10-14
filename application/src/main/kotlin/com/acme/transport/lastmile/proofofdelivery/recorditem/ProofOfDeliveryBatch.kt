package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery batch is in its life cycle. */
enum class ProofOfDeliveryBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery batch as the proof of delivery team keeps it. */
data class ProofOfDeliveryBatch(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var channel: String,
    var floorValue: Double,
    var segment: String,
    var region: String,
    var version: Int,
    var budget: Double,
    var ratio: Double,
    var status: ProofOfDeliveryBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryBatchStatus): Boolean {
        when (status) {
            ProofOfDeliveryBatchStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliveryBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryBatchStatus.APPROVED -> {
                when (next) {
                    ProofOfDeliveryBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryBatchStatus.REJECTED -> {
                when (next) {
                    ProofOfDeliveryBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryBatchStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"proof of delivery batch"} $code"

    fun weightedVolume(): Double = round((budget + version.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryBatch {
        return ProofOfDeliveryBatch(
            code = code,
            label = label,
            pinned = pinned,
            channel = channel,
            floorValue = floorValue,
            segment = segment,
            region = region,
            version = version,
            budget = budget,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a proof of delivery batch in status proposed. */
fun newProofOfDeliveryBatch(code: String, label: String): ProofOfDeliveryBatch {
    return ProofOfDeliveryBatch(
        code = code,
        label = label,
        pinned = false,
        channel = "",
        floorValue = 0.0,
        segment = "",
        region = "",
        version = 0,
        budget = 0.0,
        ratio = 0.0,
        status = ProofOfDeliveryBatchStatus.PROPOSED,
    )
}
