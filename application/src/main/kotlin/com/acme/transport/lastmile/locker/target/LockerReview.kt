package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker review is in its life cycle. */
enum class LockerReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a locker review and its current status. */
data class LockerReview(
    var code: String,
    var label: String,
    var segment: String,
    var batchSize: Int,
    var minimum: Double,
    var description: String,
    var maximum: Double,
    var loadKg: Double,
    var sequence: Int,
    var graceDays: Int,
    var quantity: Int,
    var status: LockerReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerReviewStatus): Boolean {
        when (status) {
            LockerReviewStatus.PROPOSED -> {
                when (next) {
                    LockerReviewStatus.APPROVED -> return true
                }
                return false
            }
            LockerReviewStatus.APPROVED -> {
                when (next) {
                    LockerReviewStatus.REJECTED -> return true
                }
                return false
            }
            LockerReviewStatus.REJECTED -> {
                when (next) {
                    LockerReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerReviewStatus) {
        if (!canMoveTo(next)) {
            error("locker review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((quantity.toDouble() + maximum) * 100) / 100

    fun snapshot(): LockerReview {
        return LockerReview(
            code = code,
            label = label,
            segment = segment,
            batchSize = batchSize,
            minimum = minimum,
            description = description,
            maximum = maximum,
            loadKg = loadKg,
            sequence = sequence,
            graceDays = graceDays,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a locker review in status proposed. */
fun newLockerReview(code: String, label: String): LockerReview {
    return LockerReview(
        code = code,
        label = label,
        segment = "",
        batchSize = 0,
        minimum = 0.0,
        description = "",
        maximum = 0.0,
        loadKg = 0.0,
        sequence = 0,
        graceDays = 0,
        quantity = 0,
        status = LockerReviewStatus.PROPOSED,
    )
}
