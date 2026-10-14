package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.text.normalize

/** Where a rolling defect review is in its life cycle. */
enum class RollingDefectReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling defect review, with what the planners need to act on it. */
data class RollingDefectReview(
    var code: String,
    var title: String,
    var weight: Double,
    var externalRef: String,
    var ownerId: String,
    var status: RollingDefectReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDefectReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDefectReviewStatus): Boolean {
        when (status) {
            RollingDefectReviewStatus.CREATED -> return next == RollingDefectReviewStatus.VALIDATED || next == RollingDefectReviewStatus.PUBLISHED
            RollingDefectReviewStatus.VALIDATED -> return next == RollingDefectReviewStatus.PUBLISHED || next == RollingDefectReviewStatus.RETIRED
            RollingDefectReviewStatus.PUBLISHED -> {
                when (next) {
                    RollingDefectReviewStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDefectReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling defect review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling defect review"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): RollingDefectReview {
        return RollingDefectReview(
            code = code,
            title = title,
            weight = weight,
            externalRef = externalRef,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling defect review in status created. */
fun newRollingDefectReview(code: String, title: String): RollingDefectReview {
    return RollingDefectReview(
        code = code,
        title = title,
        weight = 0.0,
        externalRef = "",
        ownerId = "",
        status = RollingDefectReviewStatus.CREATED,
    )
}
