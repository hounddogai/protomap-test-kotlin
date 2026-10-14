package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped violation batch records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a capped violation batch is in its life cycle. */
enum class CappedViolationBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capped violation batch, with what the planners need to act on it. */
data class CappedViolationBatch(
    var code: String,
    var label: String,
    var priority: Int,
    var minimum: Double,
    var floorValue: Double,
    var etaMinutes: Int,
    var attempts: Int,
    var status: CappedViolationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedViolationBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedViolationBatchStatus): Boolean {
        when (status) {
            CappedViolationBatchStatus.PROPOSED -> return next == CappedViolationBatchStatus.APPROVED || next == CappedViolationBatchStatus.WITHDRAWN
            CappedViolationBatchStatus.APPROVED -> return next == CappedViolationBatchStatus.REJECTED
            CappedViolationBatchStatus.REJECTED -> return next == CappedViolationBatchStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedViolationBatchStatus) {
        if (!canMoveTo(next)) {
            error("capped violation batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round((priority.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CappedViolationBatch {
        return CappedViolationBatch(
            code = code,
            label = label,
            priority = priority,
            minimum = minimum,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped violation batch in status proposed. */
fun newCappedViolationBatch(code: String, label: String): CappedViolationBatch {
    return CappedViolationBatch(
        code = code,
        label = label,
        priority = 0,
        minimum = 0.0,
        floorValue = 0.0,
        etaMinutes = 0,
        attempts = 0,
        status = CappedViolationBatchStatus.PROPOSED,
    )
}
