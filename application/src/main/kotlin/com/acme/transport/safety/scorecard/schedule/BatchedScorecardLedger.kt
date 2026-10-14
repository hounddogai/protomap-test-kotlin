package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched scorecard ledger is in its life cycle. */
enum class BatchedScorecardLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched scorecard ledger and its current status. */
data class BatchedScorecardLedger(
    var code: String,
    var name: String,
    var budget: Double,
    var dock: String,
    var weight: Double,
    var status: BatchedScorecardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardLedgerStatus): Boolean {
        when (status) {
            BatchedScorecardLedgerStatus.NEW -> {
                when (next) {
                    BatchedScorecardLedgerStatus.RUNNING -> return true
                }
                return false
            }
            BatchedScorecardLedgerStatus.QUEUED -> {
                when (next) {
                    BatchedScorecardLedgerStatus.FAILED -> return true
                }
                return false
            }
            BatchedScorecardLedgerStatus.RUNNING -> {
                when (next) {
                    BatchedScorecardLedgerStatus.DONE, BatchedScorecardLedgerStatus.FAILED -> return true
                }
                return false
            }
            BatchedScorecardLedgerStatus.DONE -> return next == BatchedScorecardLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedScorecardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((budget + weight) * 100) / 100

    fun snapshot(): BatchedScorecardLedger {
        return BatchedScorecardLedger(
            code = code,
            name = name,
            budget = budget,
            dock = dock,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched scorecard ledger in status new. */
fun newBatchedScorecardLedger(code: String, name: String): BatchedScorecardLedger {
    return BatchedScorecardLedger(
        code = code,
        name = name,
        budget = 0.0,
        dock = "",
        weight = 0.0,
        status = BatchedScorecardLedgerStatus.NEW,
    )
}
