package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict near miss review is in its life cycle. */
enum class StrictNearMissReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict near miss review as the near miss team keeps it. */
data class StrictNearMissReview(
    var code: String,
    var label: String,
    var externalRef: String,
    var pallets: Int,
    var margin: Double,
    var floorValue: Double,
    var status: StrictNearMissReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNearMissReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNearMissReviewStatus): Boolean {
        when (status) {
            StrictNearMissReviewStatus.REQUESTED -> return next == StrictNearMissReviewStatus.REVOKED
            StrictNearMissReviewStatus.GRANTED -> {
                when (next) {
                    StrictNearMissReviewStatus.REVOKED, StrictNearMissReviewStatus.EXPIRED -> return true
                }
                return false
            }
            StrictNearMissReviewStatus.REVOKED -> return next == StrictNearMissReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictNearMissReviewStatus) {
        if (!canMoveTo(next)) {
            error("strict near miss review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round(margin * floorValue * 100) / 100

    fun snapshot(): StrictNearMissReview {
        return StrictNearMissReview(
            code = code,
            label = label,
            externalRef = externalRef,
            pallets = pallets,
            margin = margin,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict near miss review in status requested. */
fun newStrictNearMissReview(code: String, label: String): StrictNearMissReview {
    return StrictNearMissReview(
        code = code,
        label = label,
        externalRef = "",
        pallets = 0,
        margin = 0.0,
        floorValue = 0.0,
        status = StrictNearMissReviewStatus.REQUESTED,
    )
}
