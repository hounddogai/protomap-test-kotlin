package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize

/** Where a weighted declaration review is in its life cycle. */
enum class WeightedDeclarationReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One weighted declaration review, with what the planners need to act on it. */
data class WeightedDeclarationReview(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var channel: String,
    var active: Boolean,
    var ownerId: String,
    var status: WeightedDeclarationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDeclarationReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDeclarationReviewStatus): Boolean {
        when (status) {
            WeightedDeclarationReviewStatus.PROPOSED -> return next == WeightedDeclarationReviewStatus.WITHDRAWN
            WeightedDeclarationReviewStatus.APPROVED -> {
                when (next) {
                    WeightedDeclarationReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedDeclarationReviewStatus.REJECTED -> return next == WeightedDeclarationReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedDeclarationReviewStatus) {
        if (!canMoveTo(next)) {
            error("weighted declaration review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted declaration review"} $code"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): WeightedDeclarationReview {
        return WeightedDeclarationReview(
            code = code,
            name = name,
            expiresAt = expiresAt,
            channel = channel,
            active = active,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted declaration review in status proposed. */
fun newWeightedDeclarationReview(code: String, name: String): WeightedDeclarationReview {
    return WeightedDeclarationReview(
        code = code,
        name = name,
        expiresAt = 0,
        channel = "",
        active = false,
        ownerId = "",
        status = WeightedDeclarationReviewStatus.PROPOSED,
    )
}
