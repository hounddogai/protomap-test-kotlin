package com.acme.transport.safety.violation.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback violation ledger is in its life cycle. */
enum class FallbackViolationLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback violation ledger. Its code is unique within a tenant. */
data class FallbackViolationLedger(
    var code: String,
    var label: String,
    var batchSize: Int,
    var sequence: Int,
    var capacity: Int,
    var cost: Double,
    var status: FallbackViolationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackViolationLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackViolationLedgerStatus): Boolean {
        when (status) {
            FallbackViolationLedgerStatus.CREATED -> return next == FallbackViolationLedgerStatus.PUBLISHED || next == FallbackViolationLedgerStatus.RETIRED
            FallbackViolationLedgerStatus.VALIDATED -> return next == FallbackViolationLedgerStatus.RETIRED
            FallbackViolationLedgerStatus.PUBLISHED -> return next == FallbackViolationLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackViolationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback violation ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((cost + capacity.toDouble()) * 100) / 100

    fun snapshot(): FallbackViolationLedger {
        return FallbackViolationLedger(
            code = code,
            label = label,
            batchSize = batchSize,
            sequence = sequence,
            capacity = capacity,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback violation ledger in status created. */
fun newFallbackViolationLedger(code: String, label: String): FallbackViolationLedger {
    return FallbackViolationLedger(
        code = code,
        label = label,
        batchSize = 0,
        sequence = 0,
        capacity = 0,
        cost = 0.0,
        status = FallbackViolationLedgerStatus.CREATED,
    )
}
