package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted scorecard review is in its life cycle. */
enum class WeightedScorecardReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted scorecard review as the scorecard team keeps it. */
data class WeightedScorecardReview(
    var code: String,
    var name: String,
    var ratio: Double,
    var batchSize: Int,
    var carrier: String,
    var notes: String,
    var visible: Boolean,
    var pinned: Boolean,
    var title: String,
    var status: WeightedScorecardReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedScorecardReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedScorecardReviewStatus): Boolean {
        when (status) {
            WeightedScorecardReviewStatus.DRAFT -> return next == WeightedScorecardReviewStatus.ACTIVE || next == WeightedScorecardReviewStatus.CLOSED
            WeightedScorecardReviewStatus.PENDING -> {
                when (next) {
                    WeightedScorecardReviewStatus.ACTIVE, WeightedScorecardReviewStatus.CLOSED -> return true
                }
                return false
            }
            WeightedScorecardReviewStatus.ACTIVE -> {
                when (next) {
                    WeightedScorecardReviewStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedScorecardReviewStatus.SUSPENDED -> {
                when (next) {
                    WeightedScorecardReviewStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedScorecardReviewStatus) {
        if (!canMoveTo(next)) {
            error("weighted scorecard review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((batchSize.toDouble() - ratio) * 100) / 100

    fun snapshot(): WeightedScorecardReview {
        return WeightedScorecardReview(
            code = code,
            name = name,
            ratio = ratio,
            batchSize = batchSize,
            carrier = carrier,
            notes = notes,
            visible = visible,
            pinned = pinned,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted scorecard review in status draft. */
fun newWeightedScorecardReview(code: String, name: String): WeightedScorecardReview {
    return WeightedScorecardReview(
        code = code,
        name = name,
        ratio = 0.0,
        batchSize = 0,
        carrier = "",
        notes = "",
        visible = false,
        pinned = false,
        title = "",
        status = WeightedScorecardReviewStatus.DRAFT,
    )
}
