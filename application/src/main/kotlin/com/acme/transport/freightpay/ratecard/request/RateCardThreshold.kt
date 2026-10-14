package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.normalize

/** Where a rate card threshold is in its life cycle. */
enum class RateCardThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rate card threshold, with what the planners need to act on it. */
data class RateCardThreshold(
    var code: String,
    var title: String,
    var archived: Boolean,
    var depot: String,
    var threshold: Double,
    var shiftCode: String,
    var status: RateCardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardThresholdStatus): Boolean {
        when (status) {
            RateCardThresholdStatus.NEW -> {
                when (next) {
                    RateCardThresholdStatus.QUEUED, RateCardThresholdStatus.RUNNING -> return true
                }
                return false
            }
            RateCardThresholdStatus.QUEUED -> {
                when (next) {
                    RateCardThresholdStatus.RUNNING, RateCardThresholdStatus.DONE -> return true
                }
                return false
            }
            RateCardThresholdStatus.RUNNING -> return next == RateCardThresholdStatus.DONE || next == RateCardThresholdStatus.FAILED
            RateCardThresholdStatus.DONE -> return next == RateCardThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RateCardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rate card threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rate card threshold"} $code"

    fun measuredValue(): Double = threshold

    fun snapshot(): RateCardThreshold {
        return RateCardThreshold(
            code = code,
            title = title,
            archived = archived,
            depot = depot,
            threshold = threshold,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card threshold in status new. */
fun newRateCardThreshold(code: String, title: String): RateCardThreshold {
    return RateCardThreshold(
        code = code,
        title = title,
        archived = false,
        depot = "",
        threshold = 0.0,
        shiftCode = "",
        status = RateCardThresholdStatus.NEW,
    )
}
