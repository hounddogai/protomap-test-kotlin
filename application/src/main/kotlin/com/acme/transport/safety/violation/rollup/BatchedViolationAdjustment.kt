package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched violation adjustment is in its life cycle. */
enum class BatchedViolationAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched violation adjustment as the violation team keeps it. */
data class BatchedViolationAdjustment(
    var code: String,
    var name: String,
    var externalRef: String,
    var timezone: String,
    var amount: Double,
    var summary: String,
    var carrier: String,
    var etaMinutes: Int,
    var status: BatchedViolationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedViolationAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedViolationAdjustmentStatus): Boolean {
        when (status) {
            BatchedViolationAdjustmentStatus.OPEN -> {
                when (next) {
                    BatchedViolationAdjustmentStatus.IN_REVIEW -> return true
                }
                return false
            }
            BatchedViolationAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    BatchedViolationAdjustmentStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedViolationAdjustmentStatus.RESOLVED -> return next == BatchedViolationAdjustmentStatus.REOPENED
            BatchedViolationAdjustmentStatus.REOPENED -> return next == BatchedViolationAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedViolationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("batched violation adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round((etaMinutes.toDouble() + amount) * 100) / 100

    fun snapshot(): BatchedViolationAdjustment {
        return BatchedViolationAdjustment(
            code = code,
            name = name,
            externalRef = externalRef,
            timezone = timezone,
            amount = amount,
            summary = summary,
            carrier = carrier,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched violation adjustment in status open. */
fun newBatchedViolationAdjustment(code: String, name: String): BatchedViolationAdjustment {
    return BatchedViolationAdjustment(
        code = code,
        name = name,
        externalRef = "",
        timezone = "",
        amount = 0.0,
        summary = "",
        carrier = "",
        etaMinutes = 0,
        status = BatchedViolationAdjustmentStatus.OPEN,
    )
}
