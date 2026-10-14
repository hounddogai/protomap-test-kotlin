package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global tender review records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a global tender review is in its life cycle. */
enum class GlobalTenderReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global tender review, with what the planners need to act on it. */
data class GlobalTenderReview(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var rate: Double,
    var unit: String,
    var version: Int,
    var createdAt: Int,
    var stopCount: Int,
    var endsAt: Int,
    var discount: Double,
    var status: GlobalTenderReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderReviewStatus): Boolean {
        when (status) {
            GlobalTenderReviewStatus.PROPOSED -> return next == GlobalTenderReviewStatus.WITHDRAWN
            GlobalTenderReviewStatus.APPROVED -> {
                when (next) {
                    GlobalTenderReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalTenderReviewStatus.REJECTED -> return next == GlobalTenderReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalTenderReviewStatus) {
        if (!canMoveTo(next)) {
            error("global tender review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((stopCount.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalTenderReview {
        return GlobalTenderReview(
            code = code,
            title = title,
            expiresAt = expiresAt,
            rate = rate,
            unit = unit,
            version = version,
            createdAt = createdAt,
            stopCount = stopCount,
            endsAt = endsAt,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global tender review in status proposed. */
fun newGlobalTenderReview(code: String, title: String): GlobalTenderReview {
    return GlobalTenderReview(
        code = code,
        title = title,
        expiresAt = 0,
        rate = 0.0,
        unit = "",
        version = 0,
        createdAt = 0,
        stopCount = 0,
        endsAt = 0,
        discount = 0.0,
        status = GlobalTenderReviewStatus.PROPOSED,
    )
}
