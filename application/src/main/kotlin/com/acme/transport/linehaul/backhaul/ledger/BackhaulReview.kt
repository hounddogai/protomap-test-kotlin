package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul review is in its life cycle. */
enum class BackhaulReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A backhaul review. Its code is unique within a tenant. */
data class BackhaulReview(
    var code: String,
    var title: String,
    var margin: Double,
    var sequence: Int,
    var floorValue: Double,
    var status: BackhaulReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulReviewStatus): Boolean {
        when (status) {
            BackhaulReviewStatus.REQUESTED -> {
                when (next) {
                    BackhaulReviewStatus.REVOKED, BackhaulReviewStatus.EXPIRED -> return true
                }
                return false
            }
            BackhaulReviewStatus.GRANTED -> {
                when (next) {
                    BackhaulReviewStatus.REVOKED, BackhaulReviewStatus.EXPIRED -> return true
                }
                return false
            }
            BackhaulReviewStatus.REVOKED -> return next == BackhaulReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BackhaulReviewStatus) {
        if (!canMoveTo(next)) {
            error("backhaul review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"backhaul review"} $code"

    fun netLoad(): Double = round(sequence.toDouble() * margin * 100) / 100

    fun snapshot(): BackhaulReview {
        return BackhaulReview(
            code = code,
            title = title,
            margin = margin,
            sequence = sequence,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a backhaul review in status requested. */
fun newBackhaulReview(code: String, title: String): BackhaulReview {
    return BackhaulReview(
        code = code,
        title = title,
        margin = 0.0,
        sequence = 0,
        floorValue = 0.0,
        status = BackhaulReviewStatus.REQUESTED,
    )
}
