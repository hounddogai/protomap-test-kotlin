package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched proof of delivery rollup records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a batched proof of delivery rollup is in its life cycle. */
enum class BatchedProofOfDeliveryRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched proof of delivery rollup as the proof of delivery team keeps it. */
data class BatchedProofOfDeliveryRollup(
    var code: String,
    var title: String,
    var retries: Int,
    var unit: String,
    var durationMinutes: Int,
    var sequence: Int,
    var maximum: Double,
    var status: BatchedProofOfDeliveryRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryRollupStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryRollupStatus.DRAFT -> {
                when (next) {
                    BatchedProofOfDeliveryRollupStatus.ACTIVE, BatchedProofOfDeliveryRollupStatus.CLOSED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryRollupStatus.PENDING -> return next == BatchedProofOfDeliveryRollupStatus.ACTIVE || next == BatchedProofOfDeliveryRollupStatus.SUSPENDED
            BatchedProofOfDeliveryRollupStatus.ACTIVE -> {
                when (next) {
                    BatchedProofOfDeliveryRollupStatus.SUSPENDED, BatchedProofOfDeliveryRollupStatus.CLOSED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryRollupStatus.SUSPENDED -> return next == BatchedProofOfDeliveryRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryRollupStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched proof of delivery rollup"} $code"

    fun netVolume(): Double = round(retries.toDouble() * maximum * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryRollup {
        return BatchedProofOfDeliveryRollup(
            code = code,
            title = title,
            retries = retries,
            unit = unit,
            durationMinutes = durationMinutes,
            sequence = sequence,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched proof of delivery rollup in status draft. */
fun newBatchedProofOfDeliveryRollup(code: String, title: String): BatchedProofOfDeliveryRollup {
    return BatchedProofOfDeliveryRollup(
        code = code,
        title = title,
        retries = 0,
        unit = "",
        durationMinutes = 0,
        sequence = 0,
        maximum = 0.0,
        status = BatchedProofOfDeliveryRollupStatus.DRAFT,
    )
}
