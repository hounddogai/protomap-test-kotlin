package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold review is in its life cycle. */
enum class HoldReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a hold review and its current status. */
data class HoldReview(
    var code: String,
    var title: String,
    var rate: Double,
    var loadKg: Double,
    var cost: Double,
    var status: HoldReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldReviewStatus): Boolean {
        when (status) {
            HoldReviewStatus.NEW -> {
                when (next) {
                    HoldReviewStatus.DONE -> return true
                }
                return false
            }
            HoldReviewStatus.QUEUED -> {
                when (next) {
                    HoldReviewStatus.RUNNING, HoldReviewStatus.DONE -> return true
                }
                return false
            }
            HoldReviewStatus.RUNNING -> return next == HoldReviewStatus.DONE || next == HoldReviewStatus.FAILED
            HoldReviewStatus.DONE -> return next == HoldReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: HoldReviewStatus) {
        if (!canMoveTo(next)) {
            error("hold review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((cost + rate) * 100) / 100

    fun snapshot(): HoldReview {
        return HoldReview(code = code, title = title, rate = rate, loadKg = loadKg, cost = cost, status = status)
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold review in status new. */
fun newHoldReview(code: String, title: String): HoldReview {
    return HoldReview(
        code = code,
        title = title,
        rate = 0.0,
        loadKg = 0.0,
        cost = 0.0,
        status = HoldReviewStatus.NEW,
    )
}
