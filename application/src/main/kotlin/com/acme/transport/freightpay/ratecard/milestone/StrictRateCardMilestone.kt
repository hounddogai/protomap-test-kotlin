package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize

/** Where a strict rate card milestone is in its life cycle. */
enum class StrictRateCardMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One strict rate card milestone, with what the planners need to act on it. */
data class StrictRateCardMilestone(
    var code: String,
    var label: String,
    var depot: String,
    var vehicleId: String,
    var source: String,
    var amount: Double,
    var status: StrictRateCardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardMilestoneStatus): Boolean {
        when (status) {
            StrictRateCardMilestoneStatus.REQUESTED -> {
                when (next) {
                    StrictRateCardMilestoneStatus.GRANTED -> return true
                }
                return false
            }
            StrictRateCardMilestoneStatus.GRANTED -> {
                when (next) {
                    StrictRateCardMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            StrictRateCardMilestoneStatus.REVOKED -> return next == StrictRateCardMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictRateCardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): StrictRateCardMilestone {
        return StrictRateCardMilestone(
            code = code,
            label = label,
            depot = depot,
            vehicleId = vehicleId,
            source = source,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict rate card milestone in status requested. */
fun newStrictRateCardMilestone(code: String, label: String): StrictRateCardMilestone {
    return StrictRateCardMilestone(
        code = code,
        label = label,
        depot = "",
        vehicleId = "",
        source = "",
        amount = 0.0,
        status = StrictRateCardMilestoneStatus.REQUESTED,
    )
}
