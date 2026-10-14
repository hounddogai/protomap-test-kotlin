package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dwell review is in its life cycle. */
enum class LenientDwellReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient dwell review, with what the planners need to act on it. */
data class LenientDwellReview(
    var code: String,
    var label: String,
    var pallets: Int,
    var threshold: Double,
    var minimum: Double,
    var reviewer: String,
    var depot: String,
    var fuelLiters: Double,
    var active: Boolean,
    var visible: Boolean,
    var status: LenientDwellReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellReviewStatus): Boolean {
        when (status) {
            LenientDwellReviewStatus.PLANNED -> {
                when (next) {
                    LenientDwellReviewStatus.SCHEDULED, LenientDwellReviewStatus.COMPLETED -> return true
                }
                return false
            }
            LenientDwellReviewStatus.SCHEDULED -> {
                when (next) {
                    LenientDwellReviewStatus.COMPLETED, LenientDwellReviewStatus.CANCELLED -> return true
                }
                return false
            }
            LenientDwellReviewStatus.IN_PROGRESS -> return next == LenientDwellReviewStatus.CANCELLED
            LenientDwellReviewStatus.COMPLETED -> return next == LenientDwellReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientDwellReviewStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient dwell review"} $code"

    fun projectedVolume(): Double = round((fuelLiters - minimum) * 100) / 100

    fun snapshot(): LenientDwellReview {
        return LenientDwellReview(
            code = code,
            label = label,
            pallets = pallets,
            threshold = threshold,
            minimum = minimum,
            reviewer = reviewer,
            depot = depot,
            fuelLiters = fuelLiters,
            active = active,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient dwell review in status planned. */
fun newLenientDwellReview(code: String, label: String): LenientDwellReview {
    return LenientDwellReview(
        code = code,
        label = label,
        pallets = 0,
        threshold = 0.0,
        minimum = 0.0,
        reviewer = "",
        depot = "",
        fuelLiters = 0.0,
        active = false,
        visible = false,
        status = LenientDwellReviewStatus.PLANNED,
    )
}
