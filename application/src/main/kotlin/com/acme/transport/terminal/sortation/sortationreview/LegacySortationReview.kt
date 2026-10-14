package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy sortation review records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a legacy sortation review is in its life cycle. */
enum class LegacySortationReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy sortation review. Its code is unique within a tenant. */
data class LegacySortationReview(
    var code: String,
    var title: String,
    var limit: Int,
    var expiresAt: Int,
    var revision: Int,
    var sequence: Int,
    var category: String,
    var status: LegacySortationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySortationReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySortationReviewStatus): Boolean {
        when (status) {
            LegacySortationReviewStatus.PLANNED -> return next == LegacySortationReviewStatus.SCHEDULED || next == LegacySortationReviewStatus.COMPLETED
            LegacySortationReviewStatus.SCHEDULED -> {
                when (next) {
                    LegacySortationReviewStatus.COMPLETED, LegacySortationReviewStatus.CANCELLED -> return true
                }
                return false
            }
            LegacySortationReviewStatus.IN_PROGRESS -> return next == LegacySortationReviewStatus.COMPLETED || next == LegacySortationReviewStatus.CANCELLED
            LegacySortationReviewStatus.COMPLETED -> return next == LegacySortationReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacySortationReviewStatus) {
        if (!canMoveTo(next)) {
            error("legacy sortation review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"legacy sortation review"} $code"

    fun adjustedTotal(): Double = round(sequence.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): LegacySortationReview {
        return LegacySortationReview(
            code = code,
            title = title,
            limit = limit,
            expiresAt = expiresAt,
            revision = revision,
            sequence = sequence,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy sortation review in status planned. */
fun newLegacySortationReview(code: String, title: String): LegacySortationReview {
    return LegacySortationReview(
        code = code,
        title = title,
        limit = 0,
        expiresAt = 0,
        revision = 0,
        sequence = 0,
        category = "",
        status = LegacySortationReviewStatus.PLANNED,
    )
}
