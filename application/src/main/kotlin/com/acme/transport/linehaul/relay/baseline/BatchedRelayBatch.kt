package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched relay batch is in its life cycle. */
enum class BatchedRelayBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched relay batch. Its code is unique within a tenant. */
data class BatchedRelayBatch(
    var code: String,
    var name: String,
    var budget: Double,
    var ratio: Double,
    var maximum: Double,
    var active: Boolean,
    var contactEmail: String,
    var limit: Int,
    var discount: Double,
    var sequence: Int,
    var status: BatchedRelayBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRelayBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRelayBatchStatus): Boolean {
        when (status) {
            BatchedRelayBatchStatus.PLANNED -> return next == BatchedRelayBatchStatus.COMPLETED
            BatchedRelayBatchStatus.SCHEDULED -> return next == BatchedRelayBatchStatus.COMPLETED
            BatchedRelayBatchStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedRelayBatchStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedRelayBatchStatus.COMPLETED -> {
                when (next) {
                    BatchedRelayBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRelayBatchStatus) {
        if (!canMoveTo(next)) {
            error("batched relay batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((limit.toDouble() + ratio) * 100) / 100

    fun snapshot(): BatchedRelayBatch {
        return BatchedRelayBatch(
            code = code,
            name = name,
            budget = budget,
            ratio = ratio,
            maximum = maximum,
            active = active,
            contactEmail = contactEmail,
            limit = limit,
            discount = discount,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched relay batch in status planned. */
fun newBatchedRelayBatch(code: String, name: String): BatchedRelayBatch {
    return BatchedRelayBatch(
        code = code,
        name = name,
        budget = 0.0,
        ratio = 0.0,
        maximum = 0.0,
        active = false,
        contactEmail = "",
        limit = 0,
        discount = 0.0,
        sequence = 0,
        status = BatchedRelayBatchStatus.PLANNED,
    )
}
