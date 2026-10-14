package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched violation draft is in its life cycle. */
enum class BatchedViolationDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched violation draft as the violation team keeps it. */
data class BatchedViolationDraft(
    var code: String,
    var label: String,
    var notes: String,
    var rate: Double,
    var routeCode: String,
    var ratio: Double,
    var vehicleId: String,
    var margin: Double,
    var status: BatchedViolationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedViolationDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedViolationDraftStatus): Boolean {
        when (status) {
            BatchedViolationDraftStatus.NEW -> return next == BatchedViolationDraftStatus.QUEUED
            BatchedViolationDraftStatus.QUEUED -> return next == BatchedViolationDraftStatus.DONE || next == BatchedViolationDraftStatus.FAILED
            BatchedViolationDraftStatus.RUNNING -> {
                when (next) {
                    BatchedViolationDraftStatus.DONE -> return true
                }
                return false
            }
            BatchedViolationDraftStatus.DONE -> {
                when (next) {
                    BatchedViolationDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedViolationDraftStatus) {
        if (!canMoveTo(next)) {
            error("batched violation draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched violation draft"} $code"

    fun projectedValue(): Double = round((ratio - rate) * 100) / 100

    fun snapshot(): BatchedViolationDraft {
        return BatchedViolationDraft(
            code = code,
            label = label,
            notes = notes,
            rate = rate,
            routeCode = routeCode,
            ratio = ratio,
            vehicleId = vehicleId,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched violation draft in status new. */
fun newBatchedViolationDraft(code: String, label: String): BatchedViolationDraft {
    return BatchedViolationDraft(
        code = code,
        label = label,
        notes = "",
        rate = 0.0,
        routeCode = "",
        ratio = 0.0,
        vehicleId = "",
        margin = 0.0,
        status = BatchedViolationDraftStatus.NEW,
    )
}
