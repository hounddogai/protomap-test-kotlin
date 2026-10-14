package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize

/** Where a rate card profile is in its life cycle. */
enum class RateCardProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rate card profile. Its code is unique within a tenant. */
data class RateCardProfile(
    var code: String,
    var title: String,
    var amount: Double,
    var pinned: Boolean,
    var label: String,
    var status: RateCardProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardProfileStatus): Boolean {
        when (status) {
            RateCardProfileStatus.IDLE -> return next == RateCardProfileStatus.WARMING || next == RateCardProfileStatus.DRAINING
            RateCardProfileStatus.WARMING -> {
                when (next) {
                    RateCardProfileStatus.DRAINING -> return true
                }
                return false
            }
            RateCardProfileStatus.READY -> return next == RateCardProfileStatus.DRAINING || next == RateCardProfileStatus.STOPPED
            RateCardProfileStatus.DRAINING -> return next == RateCardProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RateCardProfileStatus) {
        if (!canMoveTo(next)) {
            error("rate card profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): RateCardProfile {
        return RateCardProfile(
            code = code,
            title = title,
            amount = amount,
            pinned = pinned,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card profile in status idle. */
fun newRateCardProfile(code: String, title: String): RateCardProfile {
    return RateCardProfile(
        code = code,
        title = title,
        amount = 0.0,
        pinned = false,
        label = "",
        status = RateCardProfileStatus.IDLE,
    )
}
