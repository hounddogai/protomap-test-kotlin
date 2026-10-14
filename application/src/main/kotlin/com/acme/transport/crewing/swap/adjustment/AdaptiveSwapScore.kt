package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive swap score is in its life cycle. */
enum class AdaptiveSwapScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive swap score. Its code is unique within a tenant. */
data class AdaptiveSwapScore(
    var code: String,
    var label: String,
    var startsAt: Int,
    var endsAt: Int,
    var cost: Double,
    var segment: String,
    var retries: Int,
    var floorValue: Double,
    var status: AdaptiveSwapScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapScoreStatus): Boolean {
        when (status) {
            AdaptiveSwapScoreStatus.DRAFT -> {
                when (next) {
                    AdaptiveSwapScoreStatus.ACTIVE -> return true
                }
                return false
            }
            AdaptiveSwapScoreStatus.PENDING -> return next == AdaptiveSwapScoreStatus.SUSPENDED || next == AdaptiveSwapScoreStatus.CLOSED
            AdaptiveSwapScoreStatus.ACTIVE -> {
                when (next) {
                    AdaptiveSwapScoreStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveSwapScoreStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveSwapScoreStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round(startsAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): AdaptiveSwapScore {
        return AdaptiveSwapScore(
            code = code,
            label = label,
            startsAt = startsAt,
            endsAt = endsAt,
            cost = cost,
            segment = segment,
            retries = retries,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive swap score in status draft. */
fun newAdaptiveSwapScore(code: String, label: String): AdaptiveSwapScore {
    return AdaptiveSwapScore(
        code = code,
        label = label,
        startsAt = 0,
        endsAt = 0,
        cost = 0.0,
        segment = "",
        retries = 0,
        floorValue = 0.0,
        status = AdaptiveSwapScoreStatus.DRAFT,
    )
}
