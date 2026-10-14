package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional clearance review is in its life cycle. */
enum class RegionalClearanceReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional clearance review, with what the planners need to act on it. */
data class RegionalClearanceReview(
    var code: String,
    var label: String,
    var driverId: String,
    var tags: MutableList<String>,
    var score: Double,
    var expiresAt: Int,
    var status: RegionalClearanceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalClearanceReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalClearanceReviewStatus): Boolean {
        when (status) {
            RegionalClearanceReviewStatus.CREATED -> {
                when (next) {
                    RegionalClearanceReviewStatus.VALIDATED -> return true
                }
                return false
            }
            RegionalClearanceReviewStatus.VALIDATED -> {
                when (next) {
                    RegionalClearanceReviewStatus.RETIRED -> return true
                }
                return false
            }
            RegionalClearanceReviewStatus.PUBLISHED -> {
                when (next) {
                    RegionalClearanceReviewStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalClearanceReviewStatus) {
        if (!canMoveTo(next)) {
            error("regional clearance review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(score * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RegionalClearanceReview {
        return RegionalClearanceReview(
            code = code,
            label = label,
            driverId = driverId,
            tags = tags,
            score = score,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional clearance review in status created. */
fun newRegionalClearanceReview(code: String, label: String): RegionalClearanceReview {
    return RegionalClearanceReview(
        code = code,
        label = label,
        driverId = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        expiresAt = 0,
        status = RegionalClearanceReviewStatus.CREATED,
    )
}
