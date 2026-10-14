package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched proof of delivery mapping is in its life cycle. */
enum class BatchedProofOfDeliveryMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One batched proof of delivery mapping, with what the planners need to act on it. */
data class BatchedProofOfDeliveryMapping(
    var code: String,
    var name: String,
    var priority: Int,
    var country: String,
    var ratio: Double,
    var archived: Boolean,
    var revision: Int,
    var status: BatchedProofOfDeliveryMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryMappingStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryMappingStatus.OPEN -> return next == BatchedProofOfDeliveryMappingStatus.IN_REVIEW
            BatchedProofOfDeliveryMappingStatus.IN_REVIEW -> {
                when (next) {
                    BatchedProofOfDeliveryMappingStatus.RESOLVED, BatchedProofOfDeliveryMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryMappingStatus.RESOLVED -> {
                when (next) {
                    BatchedProofOfDeliveryMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryMappingStatus.REOPENED -> {
                when (next) {
                    BatchedProofOfDeliveryMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryMappingStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery mapping $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((ratio + revision.toDouble()) * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryMapping {
        return BatchedProofOfDeliveryMapping(
            code = code,
            name = name,
            priority = priority,
            country = country,
            ratio = ratio,
            archived = archived,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched proof of delivery mapping in status open. */
fun newBatchedProofOfDeliveryMapping(code: String, name: String): BatchedProofOfDeliveryMapping {
    return BatchedProofOfDeliveryMapping(
        code = code,
        name = name,
        priority = 0,
        country = "",
        ratio = 0.0,
        archived = false,
        revision = 0,
        status = BatchedProofOfDeliveryMappingStatus.OPEN,
    )
}
