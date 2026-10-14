package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback declaration review is in its life cycle. */
enum class FallbackDeclarationReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback declaration review and its current status. */
data class FallbackDeclarationReview(
    var code: String,
    var name: String,
    var lane: String,
    var windowDays: Int,
    var budget: Double,
    var unit: String,
    var ownerId: String,
    var margin: Double,
    var createdAt: Int,
    var description: String,
    var status: FallbackDeclarationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDeclarationReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDeclarationReviewStatus): Boolean {
        when (status) {
            FallbackDeclarationReviewStatus.PROPOSED -> return next == FallbackDeclarationReviewStatus.REJECTED || next == FallbackDeclarationReviewStatus.WITHDRAWN
            FallbackDeclarationReviewStatus.APPROVED -> return next == FallbackDeclarationReviewStatus.REJECTED
            FallbackDeclarationReviewStatus.REJECTED -> return next == FallbackDeclarationReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDeclarationReviewStatus) {
        if (!canMoveTo(next)) {
            error("fallback declaration review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback declaration review"} $code"

    fun adjustedTotal(): Double = round((margin + budget) * 100) / 100

    fun snapshot(): FallbackDeclarationReview {
        return FallbackDeclarationReview(
            code = code,
            name = name,
            lane = lane,
            windowDays = windowDays,
            budget = budget,
            unit = unit,
            ownerId = ownerId,
            margin = margin,
            createdAt = createdAt,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback declaration review in status proposed. */
fun newFallbackDeclarationReview(code: String, name: String): FallbackDeclarationReview {
    return FallbackDeclarationReview(
        code = code,
        name = name,
        lane = "",
        windowDays = 0,
        budget = 0.0,
        unit = "",
        ownerId = "",
        margin = 0.0,
        createdAt = 0,
        description = "",
        status = FallbackDeclarationReviewStatus.PROPOSED,
    )
}
