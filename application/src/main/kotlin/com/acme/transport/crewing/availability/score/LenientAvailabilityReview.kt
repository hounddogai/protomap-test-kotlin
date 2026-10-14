package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient availability review is in its life cycle. */
enum class LenientAvailabilityReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient availability review and its current status. */
data class LenientAvailabilityReview(
    var code: String,
    var label: String,
    var ceiling: Double,
    var floorValue: Double,
    var etaMinutes: Int,
    var status: LenientAvailabilityReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAvailabilityReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAvailabilityReviewStatus): Boolean {
        when (status) {
            LenientAvailabilityReviewStatus.NEW -> {
                when (next) {
                    LenientAvailabilityReviewStatus.DONE -> return true
                }
                return false
            }
            LenientAvailabilityReviewStatus.QUEUED -> return next == LenientAvailabilityReviewStatus.RUNNING || next == LenientAvailabilityReviewStatus.DONE
            LenientAvailabilityReviewStatus.RUNNING -> return next == LenientAvailabilityReviewStatus.FAILED
            LenientAvailabilityReviewStatus.DONE -> return next == LenientAvailabilityReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientAvailabilityReviewStatus) {
        if (!canMoveTo(next)) {
            error("lenient availability review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round(etaMinutes.toDouble() * ceiling * 100) / 100

    fun snapshot(): LenientAvailabilityReview {
        return LenientAvailabilityReview(
            code = code,
            label = label,
            ceiling = ceiling,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient availability review in status new. */
fun newLenientAvailabilityReview(code: String, label: String): LenientAvailabilityReview {
    return LenientAvailabilityReview(
        code = code,
        label = label,
        ceiling = 0.0,
        floorValue = 0.0,
        etaMinutes = 0,
        status = LenientAvailabilityReviewStatus.NEW,
    )
}
