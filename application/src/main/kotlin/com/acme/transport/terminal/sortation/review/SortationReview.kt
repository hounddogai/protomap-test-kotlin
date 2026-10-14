package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation review records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation review is in its life cycle. */
enum class SortationReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a sortation review and its current status. */
data class SortationReview(
    var code: String,
    var name: String,
    var maximum: Double,
    var driverId: String,
    var region: String,
    var notes: String,
    var active: Boolean,
    var cost: Double,
    var status: SortationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationReviewStatus): Boolean {
        when (status) {
            SortationReviewStatus.IDLE -> return next == SortationReviewStatus.READY || next == SortationReviewStatus.STOPPED
            SortationReviewStatus.WARMING -> return next == SortationReviewStatus.DRAINING || next == SortationReviewStatus.STOPPED
            SortationReviewStatus.READY -> {
                when (next) {
                    SortationReviewStatus.STOPPED -> return true
                }
                return false
            }
            SortationReviewStatus.DRAINING -> {
                when (next) {
                    SortationReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationReviewStatus) {
        if (!canMoveTo(next)) {
            error("sortation review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"sortation review"} $code"

    fun weightedLoad(): Double = round((cost + maximum) * 100) / 100

    fun snapshot(): SortationReview {
        return SortationReview(
            code = code,
            name = name,
            maximum = maximum,
            driverId = driverId,
            region = region,
            notes = notes,
            active = active,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a sortation review in status idle. */
fun newSortationReview(code: String, name: String): SortationReview {
    return SortationReview(
        code = code,
        name = name,
        maximum = 0.0,
        driverId = "",
        region = "",
        notes = "",
        active = false,
        cost = 0.0,
        status = SortationReviewStatus.IDLE,
    )
}
