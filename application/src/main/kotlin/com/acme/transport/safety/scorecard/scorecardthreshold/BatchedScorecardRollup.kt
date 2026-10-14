package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched scorecard rollup is in its life cycle. */
enum class BatchedScorecardRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched scorecard rollup. Its code is unique within a tenant. */
data class BatchedScorecardRollup(
    var code: String,
    var label: String,
    var margin: Double,
    var score: Double,
    var endsAt: Int,
    var batchSize: Int,
    var ownerId: String,
    var floorValue: Double,
    var timezone: String,
    var shiftCode: String,
    var contactEmail: String,
    var status: BatchedScorecardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardRollupStatus): Boolean {
        when (status) {
            BatchedScorecardRollupStatus.IDLE -> {
                when (next) {
                    BatchedScorecardRollupStatus.READY -> return true
                }
                return false
            }
            BatchedScorecardRollupStatus.WARMING -> {
                when (next) {
                    BatchedScorecardRollupStatus.READY, BatchedScorecardRollupStatus.STOPPED -> return true
                }
                return false
            }
            BatchedScorecardRollupStatus.READY -> {
                when (next) {
                    BatchedScorecardRollupStatus.DRAINING, BatchedScorecardRollupStatus.STOPPED -> return true
                }
                return false
            }
            BatchedScorecardRollupStatus.DRAINING -> return next == BatchedScorecardRollupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedScorecardRollupStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched scorecard rollup"} $code"

    fun netValue(): Double = round((endsAt.toDouble() - margin) * 100) / 100

    fun snapshot(): BatchedScorecardRollup {
        return BatchedScorecardRollup(
            code = code,
            label = label,
            margin = margin,
            score = score,
            endsAt = endsAt,
            batchSize = batchSize,
            ownerId = ownerId,
            floorValue = floorValue,
            timezone = timezone,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched scorecard rollup in status idle. */
fun newBatchedScorecardRollup(code: String, label: String): BatchedScorecardRollup {
    return BatchedScorecardRollup(
        code = code,
        label = label,
        margin = 0.0,
        score = 0.0,
        endsAt = 0,
        batchSize = 0,
        ownerId = "",
        floorValue = 0.0,
        timezone = "",
        shiftCode = "",
        contactEmail = "",
        status = BatchedScorecardRollupStatus.IDLE,
    )
}
