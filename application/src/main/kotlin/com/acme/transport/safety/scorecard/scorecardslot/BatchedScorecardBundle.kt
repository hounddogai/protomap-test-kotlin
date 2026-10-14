package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched scorecard bundle is in its life cycle. */
enum class BatchedScorecardBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched scorecard bundle. Its code is unique within a tenant. */
data class BatchedScorecardBundle(
    var code: String,
    var title: String,
    var margin: Double,
    var windowDays: Int,
    var segment: String,
    var status: BatchedScorecardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardBundleStatus): Boolean {
        when (status) {
            BatchedScorecardBundleStatus.CREATED -> {
                when (next) {
                    BatchedScorecardBundleStatus.PUBLISHED, BatchedScorecardBundleStatus.RETIRED -> return true
                }
                return false
            }
            BatchedScorecardBundleStatus.VALIDATED -> return next == BatchedScorecardBundleStatus.PUBLISHED
            BatchedScorecardBundleStatus.PUBLISHED -> return next == BatchedScorecardBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedScorecardBundleStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched scorecard bundle"} $code"

    fun adjustedValue(): Double = round((windowDays.toDouble() - margin) * 100) / 100

    fun snapshot(): BatchedScorecardBundle {
        return BatchedScorecardBundle(
            code = code,
            title = title,
            margin = margin,
            windowDays = windowDays,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched scorecard bundle in status created. */
fun newBatchedScorecardBundle(code: String, title: String): BatchedScorecardBundle {
    return BatchedScorecardBundle(
        code = code,
        title = title,
        margin = 0.0,
        windowDays = 0,
        segment = "",
        status = BatchedScorecardBundleStatus.CREATED,
    )
}
