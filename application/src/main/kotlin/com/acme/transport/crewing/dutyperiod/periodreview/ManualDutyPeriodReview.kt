package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual duty period review records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a manual duty period review is in its life cycle. */
enum class ManualDutyPeriodReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One manual duty period review, with what the planners need to act on it. */
data class ManualDutyPeriodReview(
    var code: String,
    var title: String,
    var discount: Double,
    var fuelLiters: Double,
    var attempts: Int,
    var pallets: Int,
    var sequence: Int,
    var unit: String,
    var status: ManualDutyPeriodReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodReviewStatus): Boolean {
        when (status) {
            ManualDutyPeriodReviewStatus.IDLE -> return next == ManualDutyPeriodReviewStatus.READY
            ManualDutyPeriodReviewStatus.WARMING -> {
                when (next) {
                    ManualDutyPeriodReviewStatus.READY -> return true
                }
                return false
            }
            ManualDutyPeriodReviewStatus.READY -> {
                when (next) {
                    ManualDutyPeriodReviewStatus.STOPPED -> return true
                }
                return false
            }
            ManualDutyPeriodReviewStatus.DRAINING -> {
                when (next) {
                    ManualDutyPeriodReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodReviewStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round(pallets.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): ManualDutyPeriodReview {
        return ManualDutyPeriodReview(
            code = code,
            title = title,
            discount = discount,
            fuelLiters = fuelLiters,
            attempts = attempts,
            pallets = pallets,
            sequence = sequence,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual duty period review in status idle. */
fun newManualDutyPeriodReview(code: String, title: String): ManualDutyPeriodReview {
    return ManualDutyPeriodReview(
        code = code,
        title = title,
        discount = 0.0,
        fuelLiters = 0.0,
        attempts = 0,
        pallets = 0,
        sequence = 0,
        unit = "",
        status = ManualDutyPeriodReviewStatus.IDLE,
    )
}
