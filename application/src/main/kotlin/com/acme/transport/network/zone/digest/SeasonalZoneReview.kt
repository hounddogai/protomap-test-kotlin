package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal zone review is in its life cycle. */
enum class SeasonalZoneReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal zone review and its current status. */
data class SeasonalZoneReview(
    var code: String,
    var name: String,
    var attempts: Int,
    var maximum: Double,
    var margin: Double,
    var region: String,
    var currency: String,
    var status: SeasonalZoneReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalZoneReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalZoneReviewStatus): Boolean {
        when (status) {
            SeasonalZoneReviewStatus.REQUESTED -> {
                when (next) {
                    SeasonalZoneReviewStatus.GRANTED -> return true
                }
                return false
            }
            SeasonalZoneReviewStatus.GRANTED -> return next == SeasonalZoneReviewStatus.REVOKED
            SeasonalZoneReviewStatus.REVOKED -> return next == SeasonalZoneReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalZoneReviewStatus) {
        if (!canMoveTo(next)) {
            error("seasonal zone review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal zone review"} $code"

    fun projectedTotal(): Double = round((margin - attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalZoneReview {
        return SeasonalZoneReview(
            code = code,
            name = name,
            attempts = attempts,
            maximum = maximum,
            margin = margin,
            region = region,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal zone review in status requested. */
fun newSeasonalZoneReview(code: String, name: String): SeasonalZoneReview {
    return SeasonalZoneReview(
        code = code,
        name = name,
        attempts = 0,
        maximum = 0.0,
        margin = 0.0,
        region = "",
        currency = "",
        status = SeasonalZoneReviewStatus.REQUESTED,
    )
}
