package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched emission factor batch is in its life cycle. */
enum class BatchedEmissionFactorBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched emission factor batch. Its code is unique within a tenant. */
data class BatchedEmissionFactorBatch(
    var code: String,
    var name: String,
    var floorValue: Double,
    var threshold: Double,
    var margin: Double,
    var limit: Int,
    var summary: String,
    var depot: String,
    var windowDays: Int,
    var status: BatchedEmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEmissionFactorBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEmissionFactorBatchStatus): Boolean {
        when (status) {
            BatchedEmissionFactorBatchStatus.DRAFT -> {
                when (next) {
                    BatchedEmissionFactorBatchStatus.ACTIVE -> return true
                }
                return false
            }
            BatchedEmissionFactorBatchStatus.PENDING -> {
                when (next) {
                    BatchedEmissionFactorBatchStatus.ACTIVE, BatchedEmissionFactorBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedEmissionFactorBatchStatus.ACTIVE -> return next == BatchedEmissionFactorBatchStatus.SUSPENDED
            BatchedEmissionFactorBatchStatus.SUSPENDED -> {
                when (next) {
                    BatchedEmissionFactorBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedEmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("batched emission factor batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched emission factor batch"} $code"

    fun effectiveValue(): Double = round(threshold * floorValue * 100) / 100

    fun snapshot(): BatchedEmissionFactorBatch {
        return BatchedEmissionFactorBatch(
            code = code,
            name = name,
            floorValue = floorValue,
            threshold = threshold,
            margin = margin,
            limit = limit,
            summary = summary,
            depot = depot,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched emission factor batch in status draft. */
fun newBatchedEmissionFactorBatch(code: String, name: String): BatchedEmissionFactorBatch {
    return BatchedEmissionFactorBatch(
        code = code,
        name = name,
        floorValue = 0.0,
        threshold = 0.0,
        margin = 0.0,
        limit = 0,
        summary = "",
        depot = "",
        windowDays = 0,
        status = BatchedEmissionFactorBatchStatus.DRAFT,
    )
}
