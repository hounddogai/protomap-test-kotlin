package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled accessorial review is in its life cycle. */
enum class ScheduledAccessorialReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled accessorial review, with what the planners need to act on it. */
data class ScheduledAccessorialReview(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var margin: Double,
    var capacity: Int,
    var quantity: Int,
    var status: ScheduledAccessorialReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAccessorialReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAccessorialReviewStatus): Boolean {
        when (status) {
            ScheduledAccessorialReviewStatus.CREATED -> return next == ScheduledAccessorialReviewStatus.VALIDATED || next == ScheduledAccessorialReviewStatus.PUBLISHED
            ScheduledAccessorialReviewStatus.VALIDATED -> {
                when (next) {
                    ScheduledAccessorialReviewStatus.PUBLISHED, ScheduledAccessorialReviewStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledAccessorialReviewStatus.PUBLISHED -> return next == ScheduledAccessorialReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledAccessorialReviewStatus) {
        if (!canMoveTo(next)) {
            error("scheduled accessorial review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled accessorial review"} $code"

    fun effectiveExposure(): Double = round((fuelLiters + quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledAccessorialReview {
        return ScheduledAccessorialReview(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            margin = margin,
            capacity = capacity,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled accessorial review in status created. */
fun newScheduledAccessorialReview(code: String, label: String): ScheduledAccessorialReview {
    return ScheduledAccessorialReview(
        code = code,
        label = label,
        fuelLiters = 0.0,
        margin = 0.0,
        capacity = 0,
        quantity = 0,
        status = ScheduledAccessorialReviewStatus.CREATED,
    )
}
