package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive rest break exception is in its life cycle. */
enum class AdaptiveRestBreakExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive rest break exception and its current status. */
data class AdaptiveRestBreakException(
    var code: String,
    var label: String,
    var discount: Double,
    var graceDays: Int,
    var endsAt: Int,
    var lane: String,
    var checksum: String,
    var trailerId: String,
    var cost: Double,
    var status: AdaptiveRestBreakExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRestBreakExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRestBreakExceptionStatus): Boolean {
        when (status) {
            AdaptiveRestBreakExceptionStatus.IDLE -> {
                when (next) {
                    AdaptiveRestBreakExceptionStatus.READY -> return true
                }
                return false
            }
            AdaptiveRestBreakExceptionStatus.WARMING -> return next == AdaptiveRestBreakExceptionStatus.DRAINING || next == AdaptiveRestBreakExceptionStatus.STOPPED
            AdaptiveRestBreakExceptionStatus.READY -> return next == AdaptiveRestBreakExceptionStatus.DRAINING
            AdaptiveRestBreakExceptionStatus.DRAINING -> {
                when (next) {
                    AdaptiveRestBreakExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRestBreakExceptionStatus) {
        if (!canMoveTo(next)) {
            error("adaptive rest break exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((discount + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRestBreakException {
        return AdaptiveRestBreakException(
            code = code,
            label = label,
            discount = discount,
            graceDays = graceDays,
            endsAt = endsAt,
            lane = lane,
            checksum = checksum,
            trailerId = trailerId,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive rest break exception in status idle. */
fun newAdaptiveRestBreakException(code: String, label: String): AdaptiveRestBreakException {
    return AdaptiveRestBreakException(
        code = code,
        label = label,
        discount = 0.0,
        graceDays = 0,
        endsAt = 0,
        lane = "",
        checksum = "",
        trailerId = "",
        cost = 0.0,
        status = AdaptiveRestBreakExceptionStatus.IDLE,
    )
}
