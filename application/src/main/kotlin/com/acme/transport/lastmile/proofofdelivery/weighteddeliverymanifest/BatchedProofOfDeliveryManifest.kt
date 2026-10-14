package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.text.normalize

// The batched proof of delivery manifest records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a batched proof of delivery manifest is in its life cycle. */
enum class BatchedProofOfDeliveryManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched proof of delivery manifest and its current status. */
data class BatchedProofOfDeliveryManifest(
    var code: String,
    var title: String,
    var country: String,
    var reviewer: String,
    var retries: Int,
    var status: BatchedProofOfDeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryManifestStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryManifestStatus.OPEN -> {
                when (next) {
                    BatchedProofOfDeliveryManifestStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryManifestStatus.IN_REVIEW -> return next == BatchedProofOfDeliveryManifestStatus.REOPENED
            BatchedProofOfDeliveryManifestStatus.RESOLVED -> return next == BatchedProofOfDeliveryManifestStatus.REOPENED || next == BatchedProofOfDeliveryManifestStatus.ARCHIVED
            BatchedProofOfDeliveryManifestStatus.REOPENED -> {
                when (next) {
                    BatchedProofOfDeliveryManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched proof of delivery manifest"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): BatchedProofOfDeliveryManifest {
        return BatchedProofOfDeliveryManifest(
            code = code,
            title = title,
            country = country,
            reviewer = reviewer,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched proof of delivery manifest in status open. */
fun newBatchedProofOfDeliveryManifest(code: String, title: String): BatchedProofOfDeliveryManifest {
    return BatchedProofOfDeliveryManifest(
        code = code,
        title = title,
        country = "",
        reviewer = "",
        retries = 0,
        status = BatchedProofOfDeliveryManifestStatus.OPEN,
    )
}
