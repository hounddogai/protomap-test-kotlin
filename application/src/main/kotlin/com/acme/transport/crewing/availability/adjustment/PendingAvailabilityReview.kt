package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending availability review is in its life cycle. */
enum class PendingAvailabilityReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending availability review. Its code is unique within a tenant. */
data class PendingAvailabilityReview(
    var code: String,
    var label: String,
    var notes: String,
    var amount: Double,
    var title: String,
    var capacity: Int,
    var windowDays: Int,
    var odometer: Int,
    var margin: Double,
    var reviewer: String,
    var status: PendingAvailabilityReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAvailabilityReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAvailabilityReviewStatus): Boolean {
        when (status) {
            PendingAvailabilityReviewStatus.PLANNED -> return next == PendingAvailabilityReviewStatus.SCHEDULED || next == PendingAvailabilityReviewStatus.CANCELLED
            PendingAvailabilityReviewStatus.SCHEDULED -> {
                when (next) {
                    PendingAvailabilityReviewStatus.CANCELLED -> return true
                }
                return false
            }
            PendingAvailabilityReviewStatus.IN_PROGRESS -> return next == PendingAvailabilityReviewStatus.COMPLETED || next == PendingAvailabilityReviewStatus.CANCELLED
            PendingAvailabilityReviewStatus.COMPLETED -> return next == PendingAvailabilityReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingAvailabilityReviewStatus) {
        if (!canMoveTo(next)) {
            error("pending availability review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((amount - margin) * 100) / 100

    fun snapshot(): PendingAvailabilityReview {
        return PendingAvailabilityReview(
            code = code,
            label = label,
            notes = notes,
            amount = amount,
            title = title,
            capacity = capacity,
            windowDays = windowDays,
            odometer = odometer,
            margin = margin,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending availability review in status planned. */
fun newPendingAvailabilityReview(code: String, label: String): PendingAvailabilityReview {
    return PendingAvailabilityReview(
        code = code,
        label = label,
        notes = "",
        amount = 0.0,
        title = "",
        capacity = 0,
        windowDays = 0,
        odometer = 0,
        margin = 0.0,
        reviewer = "",
        status = PendingAvailabilityReviewStatus.PLANNED,
    )
}
