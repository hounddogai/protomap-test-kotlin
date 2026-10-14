package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered redelivery review is in its life cycle. */
enum class TieredRedeliveryReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered redelivery review and its current status. */
data class TieredRedeliveryReview(
    var code: String,
    var label: String,
    var sequence: Int,
    var updatedAt: Int,
    var capacity: Int,
    var visible: Boolean,
    var endsAt: Int,
    var windowDays: Int,
    var ownerId: String,
    var segment: String,
    var status: TieredRedeliveryReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRedeliveryReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRedeliveryReviewStatus): Boolean {
        when (status) {
            TieredRedeliveryReviewStatus.CREATED -> {
                when (next) {
                    TieredRedeliveryReviewStatus.VALIDATED, TieredRedeliveryReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            TieredRedeliveryReviewStatus.VALIDATED -> {
                when (next) {
                    TieredRedeliveryReviewStatus.PUBLISHED, TieredRedeliveryReviewStatus.RETIRED -> return true
                }
                return false
            }
            TieredRedeliveryReviewStatus.PUBLISHED -> return next == TieredRedeliveryReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredRedeliveryReviewStatus) {
        if (!canMoveTo(next)) {
            error("tiered redelivery review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((capacity.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TieredRedeliveryReview {
        return TieredRedeliveryReview(
            code = code,
            label = label,
            sequence = sequence,
            updatedAt = updatedAt,
            capacity = capacity,
            visible = visible,
            endsAt = endsAt,
            windowDays = windowDays,
            ownerId = ownerId,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered redelivery review in status created. */
fun newTieredRedeliveryReview(code: String, label: String): TieredRedeliveryReview {
    return TieredRedeliveryReview(
        code = code,
        label = label,
        sequence = 0,
        updatedAt = 0,
        capacity = 0,
        visible = false,
        endsAt = 0,
        windowDays = 0,
        ownerId = "",
        segment = "",
        status = TieredRedeliveryReviewStatus.CREATED,
    )
}
