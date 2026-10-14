package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal declaration review is in its life cycle. */
enum class SeasonalDeclarationReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal declaration review, with what the planners need to act on it. */
data class SeasonalDeclarationReview(
    var code: String,
    var name: String,
    var shiftCode: String,
    var rate: Double,
    var limit: Int,
    var startsAt: Int,
    var category: String,
    var unit: String,
    var score: Double,
    var weight: Double,
    var status: SeasonalDeclarationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationReviewStatus): Boolean {
        when (status) {
            SeasonalDeclarationReviewStatus.PROPOSED -> {
                when (next) {
                    SeasonalDeclarationReviewStatus.REJECTED, SeasonalDeclarationReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalDeclarationReviewStatus.APPROVED -> return next == SeasonalDeclarationReviewStatus.REJECTED || next == SeasonalDeclarationReviewStatus.WITHDRAWN
            SeasonalDeclarationReviewStatus.REJECTED -> return next == SeasonalDeclarationReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationReviewStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal declaration review"} $code"

    fun effectiveTotal(): Double = round((rate + startsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDeclarationReview {
        return SeasonalDeclarationReview(
            code = code,
            name = name,
            shiftCode = shiftCode,
            rate = rate,
            limit = limit,
            startsAt = startsAt,
            category = category,
            unit = unit,
            score = score,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal declaration review in status proposed. */
fun newSeasonalDeclarationReview(code: String, name: String): SeasonalDeclarationReview {
    return SeasonalDeclarationReview(
        code = code,
        name = name,
        shiftCode = "",
        rate = 0.0,
        limit = 0,
        startsAt = 0,
        category = "",
        unit = "",
        score = 0.0,
        weight = 0.0,
        status = SeasonalDeclarationReviewStatus.PROPOSED,
    )
}
