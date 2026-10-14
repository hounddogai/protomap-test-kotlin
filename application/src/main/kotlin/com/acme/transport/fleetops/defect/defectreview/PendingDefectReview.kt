package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.normalize

// The pending defect review records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a pending defect review is in its life cycle. */
enum class PendingDefectReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending defect review, with what the planners need to act on it. */
data class PendingDefectReview(
    var code: String,
    var name: String,
    var limit: Int,
    var category: String,
    var currency: String,
    var status: PendingDefectReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDefectReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDefectReviewStatus): Boolean {
        when (status) {
            PendingDefectReviewStatus.REQUESTED -> return next == PendingDefectReviewStatus.GRANTED || next == PendingDefectReviewStatus.REVOKED
            PendingDefectReviewStatus.GRANTED -> {
                when (next) {
                    PendingDefectReviewStatus.REVOKED -> return true
                }
                return false
            }
            PendingDefectReviewStatus.REVOKED -> return next == PendingDefectReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDefectReviewStatus) {
        if (!canMoveTo(next)) {
            error("pending defect review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending defect review"} $code"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): PendingDefectReview {
        return PendingDefectReview(
            code = code,
            name = name,
            limit = limit,
            category = category,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending defect review in status requested. */
fun newPendingDefectReview(code: String, name: String): PendingDefectReview {
    return PendingDefectReview(
        code = code,
        name = name,
        limit = 0,
        category = "",
        currency = "",
        status = PendingDefectReviewStatus.REQUESTED,
    )
}
