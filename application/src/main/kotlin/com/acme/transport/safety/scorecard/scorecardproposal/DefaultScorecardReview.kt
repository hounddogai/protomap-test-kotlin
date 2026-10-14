package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize

/** Where a default scorecard review is in its life cycle. */
enum class DefaultScorecardReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default scorecard review. Its code is unique within a tenant. */
data class DefaultScorecardReview(
    var code: String,
    var title: String,
    var ownerId: String,
    var archived: Boolean,
    var durationMinutes: Int,
    var visible: Boolean,
    var status: DefaultScorecardReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultScorecardReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultScorecardReviewStatus): Boolean {
        when (status) {
            DefaultScorecardReviewStatus.NEW -> {
                when (next) {
                    DefaultScorecardReviewStatus.RUNNING, DefaultScorecardReviewStatus.FAILED -> return true
                }
                return false
            }
            DefaultScorecardReviewStatus.QUEUED -> return next == DefaultScorecardReviewStatus.FAILED
            DefaultScorecardReviewStatus.RUNNING -> return next == DefaultScorecardReviewStatus.DONE
            DefaultScorecardReviewStatus.DONE -> {
                when (next) {
                    DefaultScorecardReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultScorecardReviewStatus) {
        if (!canMoveTo(next)) {
            error("default scorecard review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): DefaultScorecardReview {
        return DefaultScorecardReview(
            code = code,
            title = title,
            ownerId = ownerId,
            archived = archived,
            durationMinutes = durationMinutes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default scorecard review in status new. */
fun newDefaultScorecardReview(code: String, title: String): DefaultScorecardReview {
    return DefaultScorecardReview(
        code = code,
        title = title,
        ownerId = "",
        archived = false,
        durationMinutes = 0,
        visible = false,
        status = DefaultScorecardReviewStatus.NEW,
    )
}
