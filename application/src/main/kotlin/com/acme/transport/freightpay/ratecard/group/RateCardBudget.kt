package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize

/** Where a rate card budget is in its life cycle. */
enum class RateCardBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rate card budget and its current status. */
data class RateCardBudget(
    var code: String,
    var title: String,
    var phone: String,
    var endsAt: Int,
    var segment: String,
    var status: RateCardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardBudgetStatus): Boolean {
        when (status) {
            RateCardBudgetStatus.IDLE -> {
                when (next) {
                    RateCardBudgetStatus.WARMING, RateCardBudgetStatus.DRAINING -> return true
                }
                return false
            }
            RateCardBudgetStatus.WARMING -> return next == RateCardBudgetStatus.DRAINING
            RateCardBudgetStatus.READY -> {
                when (next) {
                    RateCardBudgetStatus.DRAINING -> return true
                }
                return false
            }
            RateCardBudgetStatus.DRAINING -> return next == RateCardBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RateCardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("rate card budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rate card budget"} $code"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): RateCardBudget {
        return RateCardBudget(
            code = code,
            title = title,
            phone = phone,
            endsAt = endsAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rate card budget in status idle. */
fun newRateCardBudget(code: String, title: String): RateCardBudget {
    return RateCardBudget(
        code = code,
        title = title,
        phone = "",
        endsAt = 0,
        segment = "",
        status = RateCardBudgetStatus.IDLE,
    )
}
