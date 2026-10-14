package com.acme.transport.freightpay.audit.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback audit review is in its life cycle. */
enum class FallbackAuditReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback audit review. Its code is unique within a tenant. */
data class FallbackAuditReview(
    var code: String,
    var title: String,
    var ceiling: Double,
    var cost: Double,
    var retries: Int,
    var floorValue: Double,
    var description: String,
    var minimum: Double,
    var createdAt: Int,
    var pallets: Int,
    var status: FallbackAuditReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAuditReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAuditReviewStatus): Boolean {
        when (status) {
            FallbackAuditReviewStatus.OPEN -> return next == FallbackAuditReviewStatus.IN_REVIEW || next == FallbackAuditReviewStatus.RESOLVED
            FallbackAuditReviewStatus.IN_REVIEW -> return next == FallbackAuditReviewStatus.ARCHIVED
            FallbackAuditReviewStatus.RESOLVED -> {
                when (next) {
                    FallbackAuditReviewStatus.REOPENED, FallbackAuditReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackAuditReviewStatus.REOPENED -> return next == FallbackAuditReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackAuditReviewStatus) {
        if (!canMoveTo(next)) {
            error("fallback audit review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round(pallets.toDouble() * floorValue * 100) / 100

    fun snapshot(): FallbackAuditReview {
        return FallbackAuditReview(
            code = code,
            title = title,
            ceiling = ceiling,
            cost = cost,
            retries = retries,
            floorValue = floorValue,
            description = description,
            minimum = minimum,
            createdAt = createdAt,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback audit review in status open. */
fun newFallbackAuditReview(code: String, title: String): FallbackAuditReview {
    return FallbackAuditReview(
        code = code,
        title = title,
        ceiling = 0.0,
        cost = 0.0,
        retries = 0,
        floorValue = 0.0,
        description = "",
        minimum = 0.0,
        createdAt = 0,
        pallets = 0,
        status = FallbackAuditReviewStatus.OPEN,
    )
}
