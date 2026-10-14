package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock review is in its life cycle. */
enum class CrossDockReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock review. Its code is unique within a tenant. */
data class CrossDockReview(
    var code: String,
    var title: String,
    var unit: String,
    var pinned: Boolean,
    var rate: Double,
    var pallets: Int,
    var amount: Double,
    var status: CrossDockReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockReviewStatus): Boolean {
        when (status) {
            CrossDockReviewStatus.IDLE -> return next == CrossDockReviewStatus.WARMING
            CrossDockReviewStatus.WARMING -> {
                when (next) {
                    CrossDockReviewStatus.DRAINING -> return true
                }
                return false
            }
            CrossDockReviewStatus.READY -> {
                when (next) {
                    CrossDockReviewStatus.DRAINING, CrossDockReviewStatus.STOPPED -> return true
                }
                return false
            }
            CrossDockReviewStatus.DRAINING -> return next == CrossDockReviewStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CrossDockReviewStatus) {
        if (!canMoveTo(next)) {
            error("cross dock review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((pallets.toDouble() - amount) * 100) / 100

    fun snapshot(): CrossDockReview {
        return CrossDockReview(
            code = code,
            title = title,
            unit = unit,
            pinned = pinned,
            rate = rate,
            pallets = pallets,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a cross dock review in status idle. */
fun newCrossDockReview(code: String, title: String): CrossDockReview {
    return CrossDockReview(
        code = code,
        title = title,
        unit = "",
        pinned = false,
        rate = 0.0,
        pallets = 0,
        amount = 0.0,
        status = CrossDockReviewStatus.IDLE,
    )
}
