package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.text.normalize

/** Where a trailer review is in its life cycle. */
enum class TrailerReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One trailer review, with what the planners need to act on it. */
data class TrailerReview(
    var code: String,
    var name: String,
    var revision: Int,
    var checksum: String,
    var currency: String,
    var pinned: Boolean,
    var driverId: String,
    var status: TrailerReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerReviewStatus): Boolean {
        when (status) {
            TrailerReviewStatus.CREATED -> return next == TrailerReviewStatus.PUBLISHED || next == TrailerReviewStatus.RETIRED
            TrailerReviewStatus.VALIDATED -> return next == TrailerReviewStatus.RETIRED
            TrailerReviewStatus.PUBLISHED -> return next == TrailerReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerReviewStatus) {
        if (!canMoveTo(next)) {
            error("trailer review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"trailer review"} $code"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): TrailerReview {
        return TrailerReview(
            code = code,
            name = name,
            revision = revision,
            checksum = checksum,
            currency = currency,
            pinned = pinned,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer review in status created. */
fun newTrailerReview(code: String, name: String): TrailerReview {
    return TrailerReview(
        code = code,
        name = name,
        revision = 0,
        checksum = "",
        currency = "",
        pinned = false,
        driverId = "",
        status = TrailerReviewStatus.CREATED,
    )
}
