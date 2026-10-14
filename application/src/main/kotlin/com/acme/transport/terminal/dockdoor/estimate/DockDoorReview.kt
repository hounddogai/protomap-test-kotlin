package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door review is in its life cycle. */
enum class DockDoorReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One dock door review, with what the planners need to act on it. */
data class DockDoorReview(
    var code: String,
    var name: String,
    var limit: Int,
    var region: String,
    var threshold: Double,
    var status: DockDoorReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorReviewStatus): Boolean {
        when (status) {
            DockDoorReviewStatus.PROPOSED -> {
                when (next) {
                    DockDoorReviewStatus.APPROVED, DockDoorReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            DockDoorReviewStatus.APPROVED -> {
                when (next) {
                    DockDoorReviewStatus.REJECTED, DockDoorReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            DockDoorReviewStatus.REJECTED -> return next == DockDoorReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DockDoorReviewStatus) {
        if (!canMoveTo(next)) {
            error("dock door review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round(threshold * limit.toDouble() * 100) / 100

    fun snapshot(): DockDoorReview {
        return DockDoorReview(
            code = code,
            name = name,
            limit = limit,
            region = region,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dock door review in status proposed. */
fun newDockDoorReview(code: String, name: String): DockDoorReview {
    return DockDoorReview(
        code = code,
        name = name,
        limit = 0,
        region = "",
        threshold = 0.0,
        status = DockDoorReviewStatus.PROPOSED,
    )
}
