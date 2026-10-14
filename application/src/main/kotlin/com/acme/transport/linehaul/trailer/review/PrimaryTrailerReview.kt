package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary trailer review records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a primary trailer review is in its life cycle. */
enum class PrimaryTrailerReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary trailer review, with what the planners need to act on it. */
data class PrimaryTrailerReview(
    var code: String,
    var label: String,
    var minimum: Double,
    var phone: String,
    var version: Int,
    var status: PrimaryTrailerReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerReviewStatus): Boolean {
        when (status) {
            PrimaryTrailerReviewStatus.IDLE -> return next == PrimaryTrailerReviewStatus.DRAINING
            PrimaryTrailerReviewStatus.WARMING -> {
                when (next) {
                    PrimaryTrailerReviewStatus.READY, PrimaryTrailerReviewStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryTrailerReviewStatus.READY -> return next == PrimaryTrailerReviewStatus.DRAINING
            PrimaryTrailerReviewStatus.DRAINING -> {
                when (next) {
                    PrimaryTrailerReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerReviewStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((version.toDouble() + minimum) * 100) / 100

    fun snapshot(): PrimaryTrailerReview {
        return PrimaryTrailerReview(
            code = code,
            label = label,
            minimum = minimum,
            phone = phone,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary trailer review in status idle. */
fun newPrimaryTrailerReview(code: String, label: String): PrimaryTrailerReview {
    return PrimaryTrailerReview(
        code = code,
        label = label,
        minimum = 0.0,
        phone = "",
        version = 0,
        status = PrimaryTrailerReviewStatus.IDLE,
    )
}
