package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.text.normalize

/** Where a global attempt review is in its life cycle. */
enum class GlobalAttemptReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global attempt review, with what the planners need to act on it. */
data class GlobalAttemptReview(
    var code: String,
    var name: String,
    var routeCode: String,
    var endsAt: Int,
    var category: String,
    var status: GlobalAttemptReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAttemptReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAttemptReviewStatus): Boolean {
        when (status) {
            GlobalAttemptReviewStatus.REQUESTED -> {
                when (next) {
                    GlobalAttemptReviewStatus.GRANTED -> return true
                }
                return false
            }
            GlobalAttemptReviewStatus.GRANTED -> return next == GlobalAttemptReviewStatus.EXPIRED
            GlobalAttemptReviewStatus.REVOKED -> return next == GlobalAttemptReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalAttemptReviewStatus) {
        if (!canMoveTo(next)) {
            error("global attempt review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): GlobalAttemptReview {
        return GlobalAttemptReview(
            code = code,
            name = name,
            routeCode = routeCode,
            endsAt = endsAt,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global attempt review in status requested. */
fun newGlobalAttemptReview(code: String, name: String): GlobalAttemptReview {
    return GlobalAttemptReview(
        code = code,
        name = name,
        routeCode = "",
        endsAt = 0,
        category = "",
        status = GlobalAttemptReviewStatus.REQUESTED,
    )
}
