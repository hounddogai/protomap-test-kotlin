package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched scorecard proposal is in its life cycle. */
enum class BatchedScorecardProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched scorecard proposal as the scorecard team keeps it. */
data class BatchedScorecardProposal(
    var code: String,
    var label: String,
    var lane: String,
    var unit: String,
    var timezone: String,
    var shiftCode: String,
    var batchSize: Int,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var driverId: String,
    var status: BatchedScorecardProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardProposalStatus): Boolean {
        when (status) {
            BatchedScorecardProposalStatus.IDLE -> return next == BatchedScorecardProposalStatus.STOPPED
            BatchedScorecardProposalStatus.WARMING -> return next == BatchedScorecardProposalStatus.READY
            BatchedScorecardProposalStatus.READY -> {
                when (next) {
                    BatchedScorecardProposalStatus.DRAINING, BatchedScorecardProposalStatus.STOPPED -> return true
                }
                return false
            }
            BatchedScorecardProposalStatus.DRAINING -> return next == BatchedScorecardProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedScorecardProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched scorecard proposal"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((updatedAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): BatchedScorecardProposal {
        return BatchedScorecardProposal(
            code = code,
            label = label,
            lane = lane,
            unit = unit,
            timezone = timezone,
            shiftCode = shiftCode,
            batchSize = batchSize,
            tags = tags,
            updatedAt = updatedAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched scorecard proposal in status idle. */
fun newBatchedScorecardProposal(code: String, label: String): BatchedScorecardProposal {
    return BatchedScorecardProposal(
        code = code,
        label = label,
        lane = "",
        unit = "",
        timezone = "",
        shiftCode = "",
        batchSize = 0,
        tags = mutableListOf<String>(),
        updatedAt = 0,
        driverId = "",
        status = BatchedScorecardProposalStatus.IDLE,
    )
}
