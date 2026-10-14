package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap score records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap score is in its life cycle. */
enum class SwapScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A swap score as the swap team keeps it. */
data class SwapScore(
    var code: String,
    var name: String,
    var budget: Double,
    var endsAt: Int,
    var floorValue: Double,
    var ratio: Double,
    var segment: String,
    var quantity: Int,
    var timezone: String,
    var source: String,
    var status: SwapScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapScoreStatus): Boolean {
        when (status) {
            SwapScoreStatus.OPEN -> return next == SwapScoreStatus.IN_REVIEW || next == SwapScoreStatus.ARCHIVED
            SwapScoreStatus.IN_REVIEW -> return next == SwapScoreStatus.REOPENED
            SwapScoreStatus.RESOLVED -> return next == SwapScoreStatus.ARCHIVED
            SwapScoreStatus.REOPENED -> {
                when (next) {
                    SwapScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapScoreStatus) {
        if (!canMoveTo(next)) {
            error("swap score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((floorValue - quantity.toDouble()) * 100) / 100

    fun snapshot(): SwapScore {
        return SwapScore(
            code = code,
            name = name,
            budget = budget,
            endsAt = endsAt,
            floorValue = floorValue,
            ratio = ratio,
            segment = segment,
            quantity = quantity,
            timezone = timezone,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a swap score in status open. */
fun newSwapScore(code: String, name: String): SwapScore {
    return SwapScore(
        code = code,
        name = name,
        budget = 0.0,
        endsAt = 0,
        floorValue = 0.0,
        ratio = 0.0,
        segment = "",
        quantity = 0,
        timezone = "",
        source = "",
        status = SwapScoreStatus.OPEN,
    )
}
