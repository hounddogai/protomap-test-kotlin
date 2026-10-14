package com.acme.transport.customs.inspection.review

import com.acme.transport.kit.text.normalize

// The adaptive inspection review records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a adaptive inspection review is in its life cycle. */
enum class AdaptiveInspectionReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a adaptive inspection review and its current status. */
data class AdaptiveInspectionReview(
    var code: String,
    var name: String,
    var notes: String,
    var externalRef: String,
    var endsAt: Int,
    var status: AdaptiveInspectionReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionReviewStatus): Boolean {
        when (status) {
            AdaptiveInspectionReviewStatus.REQUESTED -> {
                when (next) {
                    AdaptiveInspectionReviewStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveInspectionReviewStatus.GRANTED -> {
                when (next) {
                    AdaptiveInspectionReviewStatus.REVOKED, AdaptiveInspectionReviewStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveInspectionReviewStatus.REVOKED -> return next == AdaptiveInspectionReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionReviewStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive inspection review"} $code"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): AdaptiveInspectionReview {
        return AdaptiveInspectionReview(
            code = code,
            name = name,
            notes = notes,
            externalRef = externalRef,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection review in status requested. */
fun newAdaptiveInspectionReview(code: String, name: String): AdaptiveInspectionReview {
    return AdaptiveInspectionReview(
        code = code,
        name = name,
        notes = "",
        externalRef = "",
        endsAt = 0,
        status = AdaptiveInspectionReviewStatus.REQUESTED,
    )
}
