package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive rest break score is in its life cycle. */
enum class AdaptiveRestBreakScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive rest break score and its current status. */
data class AdaptiveRestBreakScore(
    var code: String,
    var label: String,
    var description: String,
    var lane: String,
    var externalRef: String,
    var reviewer: String,
    var cost: Double,
    var threshold: Double,
    var phone: String,
    var amount: Double,
    var attempts: Int,
    var status: AdaptiveRestBreakScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRestBreakScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRestBreakScoreStatus): Boolean {
        when (status) {
            AdaptiveRestBreakScoreStatus.PLANNED -> return next == AdaptiveRestBreakScoreStatus.SCHEDULED || next == AdaptiveRestBreakScoreStatus.CANCELLED
            AdaptiveRestBreakScoreStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveRestBreakScoreStatus.COMPLETED, AdaptiveRestBreakScoreStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveRestBreakScoreStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveRestBreakScoreStatus.COMPLETED, AdaptiveRestBreakScoreStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveRestBreakScoreStatus.COMPLETED -> {
                when (next) {
                    AdaptiveRestBreakScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRestBreakScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive rest break score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round((threshold + attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRestBreakScore {
        return AdaptiveRestBreakScore(
            code = code,
            label = label,
            description = description,
            lane = lane,
            externalRef = externalRef,
            reviewer = reviewer,
            cost = cost,
            threshold = threshold,
            phone = phone,
            amount = amount,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive rest break score in status planned. */
fun newAdaptiveRestBreakScore(code: String, label: String): AdaptiveRestBreakScore {
    return AdaptiveRestBreakScore(
        code = code,
        label = label,
        description = "",
        lane = "",
        externalRef = "",
        reviewer = "",
        cost = 0.0,
        threshold = 0.0,
        phone = "",
        amount = 0.0,
        attempts = 0,
        status = AdaptiveRestBreakScoreStatus.PLANNED,
    )
}
