package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched trailer yard audit is in its life cycle. */
enum class BatchedTrailerYardAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched trailer yard audit, with what the planners need to act on it. */
data class BatchedTrailerYardAudit(
    var code: String,
    var label: String,
    var approved: Boolean,
    var batchSize: Int,
    var shiftCode: String,
    var quantity: Int,
    var retries: Int,
    var country: String,
    var createdAt: Int,
    var status: BatchedTrailerYardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerYardAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerYardAuditStatus): Boolean {
        when (status) {
            BatchedTrailerYardAuditStatus.DRAFT -> return next == BatchedTrailerYardAuditStatus.PENDING || next == BatchedTrailerYardAuditStatus.CLOSED
            BatchedTrailerYardAuditStatus.PENDING -> {
                when (next) {
                    BatchedTrailerYardAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedTrailerYardAuditStatus.ACTIVE -> {
                when (next) {
                    BatchedTrailerYardAuditStatus.SUSPENDED, BatchedTrailerYardAuditStatus.CLOSED -> return true
                }
                return false
            }
            BatchedTrailerYardAuditStatus.SUSPENDED -> return next == BatchedTrailerYardAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedTrailerYardAuditStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer yard audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round(retries.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): BatchedTrailerYardAudit {
        return BatchedTrailerYardAudit(
            code = code,
            label = label,
            approved = approved,
            batchSize = batchSize,
            shiftCode = shiftCode,
            quantity = quantity,
            retries = retries,
            country = country,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched trailer yard audit in status draft. */
fun newBatchedTrailerYardAudit(code: String, label: String): BatchedTrailerYardAudit {
    return BatchedTrailerYardAudit(
        code = code,
        label = label,
        approved = false,
        batchSize = 0,
        shiftCode = "",
        quantity = 0,
        retries = 0,
        country = "",
        createdAt = 0,
        status = BatchedTrailerYardAuditStatus.DRAFT,
    )
}
