package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.normalize

/** Where a adaptive clearance budget is in its life cycle. */
enum class AdaptiveClearanceBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive clearance budget, with what the planners need to act on it. */
data class AdaptiveClearanceBudget(
    var code: String,
    var label: String,
    var lane: String,
    var title: String,
    var depot: String,
    var currency: String,
    var endsAt: Int,
    var segment: String,
    var active: Boolean,
    var status: AdaptiveClearanceBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveClearanceBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveClearanceBudgetStatus): Boolean {
        when (status) {
            AdaptiveClearanceBudgetStatus.PLANNED -> {
                when (next) {
                    AdaptiveClearanceBudgetStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveClearanceBudgetStatus.SCHEDULED -> return next == AdaptiveClearanceBudgetStatus.COMPLETED
            AdaptiveClearanceBudgetStatus.IN_PROGRESS -> return next == AdaptiveClearanceBudgetStatus.COMPLETED
            AdaptiveClearanceBudgetStatus.COMPLETED -> return next == AdaptiveClearanceBudgetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveClearanceBudgetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive clearance budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive clearance budget"} $code"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): AdaptiveClearanceBudget {
        return AdaptiveClearanceBudget(
            code = code,
            label = label,
            lane = lane,
            title = title,
            depot = depot,
            currency = currency,
            endsAt = endsAt,
            segment = segment,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive clearance budget in status planned. */
fun newAdaptiveClearanceBudget(code: String, label: String): AdaptiveClearanceBudget {
    return AdaptiveClearanceBudget(
        code = code,
        label = label,
        lane = "",
        title = "",
        depot = "",
        currency = "",
        endsAt = 0,
        segment = "",
        active = false,
        status = AdaptiveClearanceBudgetStatus.PLANNED,
    )
}
