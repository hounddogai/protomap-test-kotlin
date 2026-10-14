package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell review is in its life cycle. */
enum class DwellReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dwell review. Its code is unique within a tenant. */
data class DwellReview(
    var code: String,
    var title: String,
    var priority: Int,
    var phone: String,
    var pallets: Int,
    var carrier: String,
    var endsAt: Int,
    var status: DwellReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellReviewStatus): Boolean {
        when (status) {
            DwellReviewStatus.NEW -> return next == DwellReviewStatus.QUEUED
            DwellReviewStatus.QUEUED -> {
                when (next) {
                    DwellReviewStatus.RUNNING, DwellReviewStatus.DONE -> return true
                }
                return false
            }
            DwellReviewStatus.RUNNING -> return next == DwellReviewStatus.FAILED
            DwellReviewStatus.DONE -> {
                when (next) {
                    DwellReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellReviewStatus) {
        if (!canMoveTo(next)) {
            error("dwell review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round(pallets.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): DwellReview {
        return DwellReview(
            code = code,
            title = title,
            priority = priority,
            phone = phone,
            pallets = pallets,
            carrier = carrier,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell review in status new. */
fun newDwellReview(code: String, title: String): DwellReview {
    return DwellReview(
        code = code,
        title = title,
        priority = 0,
        phone = "",
        pallets = 0,
        carrier = "",
        endsAt = 0,
        status = DwellReviewStatus.NEW,
    )
}
