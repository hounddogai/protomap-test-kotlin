package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched proof of delivery preference records of lastmile's proof of delivery work, and the rules,
// stores, and calculations around them.

/** Where a batched proof of delivery preference is in its life cycle. */
enum class BatchedProofOfDeliveryPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched proof of delivery preference. Its code is unique within a tenant. */
data class BatchedProofOfDeliveryPreference(
    var code: String,
    var label: String,
    var active: Boolean,
    var retries: Int,
    var maximum: Double,
    var fuelLiters: Double,
    var status: BatchedProofOfDeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryPreferenceStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryPreferenceStatus.PROPOSED -> {
                when (next) {
                    BatchedProofOfDeliveryPreferenceStatus.APPROVED, BatchedProofOfDeliveryPreferenceStatus.REJECTED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryPreferenceStatus.APPROVED -> return next == BatchedProofOfDeliveryPreferenceStatus.REJECTED || next == BatchedProofOfDeliveryPreferenceStatus.WITHDRAWN
            BatchedProofOfDeliveryPreferenceStatus.REJECTED -> {
                when (next) {
                    BatchedProofOfDeliveryPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round(fuelLiters * maximum * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryPreference {
        return BatchedProofOfDeliveryPreference(
            code = code,
            label = label,
            active = active,
            retries = retries,
            maximum = maximum,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched proof of delivery preference in status proposed. */
fun newBatchedProofOfDeliveryPreference(code: String, label: String): BatchedProofOfDeliveryPreference {
    return BatchedProofOfDeliveryPreference(
        code = code,
        label = label,
        active = false,
        retries = 0,
        maximum = 0.0,
        fuelLiters = 0.0,
        status = BatchedProofOfDeliveryPreferenceStatus.PROPOSED,
    )
}
