package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped inspection batch records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a capped inspection batch is in its life cycle. */
enum class CappedInspectionBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped inspection batch, with what the planners need to act on it. */
data class CappedInspectionBatch(
    var code: String,
    var label: String,
    var notes: String,
    var version: Int,
    var score: Double,
    var expiresAt: Int,
    var status: CappedInspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionBatchStatus): Boolean {
        when (status) {
            CappedInspectionBatchStatus.DRAFT -> return next == CappedInspectionBatchStatus.ACTIVE
            CappedInspectionBatchStatus.PENDING -> {
                when (next) {
                    CappedInspectionBatchStatus.ACTIVE -> return true
                }
                return false
            }
            CappedInspectionBatchStatus.ACTIVE -> return next == CappedInspectionBatchStatus.CLOSED
            CappedInspectionBatchStatus.SUSPENDED -> return next == CappedInspectionBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedInspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round(score * version.toDouble() * 100) / 100

    fun snapshot(): CappedInspectionBatch {
        return CappedInspectionBatch(
            code = code,
            label = label,
            notes = notes,
            version = version,
            score = score,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped inspection batch in status draft. */
fun newCappedInspectionBatch(code: String, label: String): CappedInspectionBatch {
    return CappedInspectionBatch(
        code = code,
        label = label,
        notes = "",
        version = 0,
        score = 0.0,
        expiresAt = 0,
        status = CappedInspectionBatchStatus.DRAFT,
    )
}
