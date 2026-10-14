package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return review is in its life cycle. */
enum class LeaseReturnReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lease return review. Its code is unique within a tenant. */
data class LeaseReturnReview(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var endsAt: Int,
    var threshold: Double,
    var segment: String,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var status: LeaseReturnReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnReviewStatus): Boolean {
        when (status) {
            LeaseReturnReviewStatus.PROPOSED -> {
                when (next) {
                    LeaseReturnReviewStatus.REJECTED -> return true
                }
                return false
            }
            LeaseReturnReviewStatus.APPROVED -> {
                when (next) {
                    LeaseReturnReviewStatus.REJECTED, LeaseReturnReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            LeaseReturnReviewStatus.REJECTED -> {
                when (next) {
                    LeaseReturnReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnReviewStatus) {
        if (!canMoveTo(next)) {
            error("lease return review $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lease return review"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((endsAt.toDouble() + threshold) * 100) / 100

    fun snapshot(): LeaseReturnReview {
        return LeaseReturnReview(
            code = code,
            title = title,
            tags = tags,
            endsAt = endsAt,
            threshold = threshold,
            segment = segment,
            routeCode = routeCode,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lease return review in status proposed. */
fun newLeaseReturnReview(code: String, title: String): LeaseReturnReview {
    return LeaseReturnReview(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        endsAt = 0,
        threshold = 0.0,
        segment = "",
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        status = LeaseReturnReviewStatus.PROPOSED,
    )
}
