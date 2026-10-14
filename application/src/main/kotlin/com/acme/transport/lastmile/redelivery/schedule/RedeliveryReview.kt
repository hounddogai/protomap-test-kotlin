package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery review is in its life cycle. */
enum class RedeliveryReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a redelivery review and its current status. */
data class RedeliveryReview(
    var code: String,
    var title: String,
    var lane: String,
    var tags: MutableList<String>,
    var pinned: Boolean,
    var margin: Double,
    var sequence: Int,
    var ownerId: String,
    var notes: String,
    var status: RedeliveryReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryReviewStatus): Boolean {
        when (status) {
            RedeliveryReviewStatus.PROPOSED -> {
                when (next) {
                    RedeliveryReviewStatus.APPROVED, RedeliveryReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            RedeliveryReviewStatus.APPROVED -> return next == RedeliveryReviewStatus.WITHDRAWN
            RedeliveryReviewStatus.REJECTED -> return next == RedeliveryReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RedeliveryReviewStatus) {
        if (!canMoveTo(next)) {
            error("redelivery review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(margin * sequence.toDouble() * 100) / 100

    fun snapshot(): RedeliveryReview {
        return RedeliveryReview(
            code = code,
            title = title,
            lane = lane,
            tags = tags,
            pinned = pinned,
            margin = margin,
            sequence = sequence,
            ownerId = ownerId,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a redelivery review in status proposed. */
fun newRedeliveryReview(code: String, title: String): RedeliveryReview {
    return RedeliveryReview(
        code = code,
        title = title,
        lane = "",
        tags = mutableListOf<String>(),
        pinned = false,
        margin = 0.0,
        sequence = 0,
        ownerId = "",
        notes = "",
        status = RedeliveryReviewStatus.PROPOSED,
    )
}
