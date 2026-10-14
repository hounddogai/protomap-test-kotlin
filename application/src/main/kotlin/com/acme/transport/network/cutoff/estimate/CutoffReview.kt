package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff review is in its life cycle. */
enum class CutoffReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A cutoff review as the cutoff team keeps it. */
data class CutoffReview(
    var code: String,
    var title: String,
    var capacity: Int,
    var minimum: Double,
    var attempts: Int,
    var fuelLiters: Double,
    var active: Boolean,
    var margin: Double,
    var startsAt: Int,
    var amount: Double,
    var score: Double,
    var status: CutoffReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffReviewStatus): Boolean {
        when (status) {
            CutoffReviewStatus.PLANNED -> return next == CutoffReviewStatus.COMPLETED
            CutoffReviewStatus.SCHEDULED -> {
                when (next) {
                    CutoffReviewStatus.COMPLETED, CutoffReviewStatus.CANCELLED -> return true
                }
                return false
            }
            CutoffReviewStatus.IN_PROGRESS -> {
                when (next) {
                    CutoffReviewStatus.CANCELLED -> return true
                }
                return false
            }
            CutoffReviewStatus.COMPLETED -> return next == CutoffReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CutoffReviewStatus) {
        if (!canMoveTo(next)) {
            error("cutoff review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((margin + fuelLiters) * 100) / 100

    fun snapshot(): CutoffReview {
        return CutoffReview(
            code = code,
            title = title,
            capacity = capacity,
            minimum = minimum,
            attempts = attempts,
            fuelLiters = fuelLiters,
            active = active,
            margin = margin,
            startsAt = startsAt,
            amount = amount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff review in status planned. */
fun newCutoffReview(code: String, title: String): CutoffReview {
    return CutoffReview(
        code = code,
        title = title,
        capacity = 0,
        minimum = 0.0,
        attempts = 0,
        fuelLiters = 0.0,
        active = false,
        margin = 0.0,
        startsAt = 0,
        amount = 0.0,
        score = 0.0,
        status = CutoffReviewStatus.PLANNED,
    )
}
