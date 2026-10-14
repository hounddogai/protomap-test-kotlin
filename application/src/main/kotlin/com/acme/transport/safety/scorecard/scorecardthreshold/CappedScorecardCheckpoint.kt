package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped scorecard checkpoint is in its life cycle. */
enum class CappedScorecardCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped scorecard checkpoint as the scorecard team keeps it. */
data class CappedScorecardCheckpoint(
    var code: String,
    var label: String,
    var ceiling: Double,
    var batchSize: Int,
    var revision: Int,
    var depot: String,
    var status: CappedScorecardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardCheckpointStatus): Boolean {
        when (status) {
            CappedScorecardCheckpointStatus.PLANNED -> return next == CappedScorecardCheckpointStatus.IN_PROGRESS || next == CappedScorecardCheckpointStatus.COMPLETED
            CappedScorecardCheckpointStatus.SCHEDULED -> {
                when (next) {
                    CappedScorecardCheckpointStatus.IN_PROGRESS, CappedScorecardCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            CappedScorecardCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    CappedScorecardCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            CappedScorecardCheckpointStatus.COMPLETED -> {
                when (next) {
                    CappedScorecardCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedScorecardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard checkpoint $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round(revision.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): CappedScorecardCheckpoint {
        return CappedScorecardCheckpoint(
            code = code,
            label = label,
            ceiling = ceiling,
            batchSize = batchSize,
            revision = revision,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped scorecard checkpoint in status planned. */
fun newCappedScorecardCheckpoint(code: String, label: String): CappedScorecardCheckpoint {
    return CappedScorecardCheckpoint(
        code = code,
        label = label,
        ceiling = 0.0,
        batchSize = 0,
        revision = 0,
        depot = "",
        status = CappedScorecardCheckpointStatus.PLANNED,
    )
}
