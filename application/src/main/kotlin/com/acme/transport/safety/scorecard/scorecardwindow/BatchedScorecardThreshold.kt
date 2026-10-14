package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched scorecard threshold is in its life cycle. */
enum class BatchedScorecardThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched scorecard threshold, with what the planners need to act on it. */
data class BatchedScorecardThreshold(
    var code: String,
    var name: String,
    var carrier: String,
    var margin: Double,
    var shiftCode: String,
    var revision: Int,
    var source: String,
    var pallets: Int,
    var batchSize: Int,
    var status: BatchedScorecardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardThresholdStatus): Boolean {
        when (status) {
            BatchedScorecardThresholdStatus.NEW -> {
                when (next) {
                    BatchedScorecardThresholdStatus.DONE -> return true
                }
                return false
            }
            BatchedScorecardThresholdStatus.QUEUED -> {
                when (next) {
                    BatchedScorecardThresholdStatus.DONE, BatchedScorecardThresholdStatus.FAILED -> return true
                }
                return false
            }
            BatchedScorecardThresholdStatus.RUNNING -> {
                when (next) {
                    BatchedScorecardThresholdStatus.DONE, BatchedScorecardThresholdStatus.FAILED -> return true
                }
                return false
            }
            BatchedScorecardThresholdStatus.DONE -> {
                when (next) {
                    BatchedScorecardThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedScorecardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round((batchSize.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): BatchedScorecardThreshold {
        return BatchedScorecardThreshold(
            code = code,
            name = name,
            carrier = carrier,
            margin = margin,
            shiftCode = shiftCode,
            revision = revision,
            source = source,
            pallets = pallets,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched scorecard threshold in status new. */
fun newBatchedScorecardThreshold(code: String, name: String): BatchedScorecardThreshold {
    return BatchedScorecardThreshold(
        code = code,
        name = name,
        carrier = "",
        margin = 0.0,
        shiftCode = "",
        revision = 0,
        source = "",
        pallets = 0,
        batchSize = 0,
        status = BatchedScorecardThresholdStatus.NEW,
    )
}
