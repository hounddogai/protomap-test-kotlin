package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rate card plan is in its life cycle. */
enum class StrictRateCardPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict rate card plan as the rate card team keeps it. */
data class StrictRateCardPlan(
    var code: String,
    var title: String,
    var pallets: Int,
    var description: String,
    var limit: Int,
    var status: StrictRateCardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardPlanStatus): Boolean {
        when (status) {
            StrictRateCardPlanStatus.DRAFT -> {
                when (next) {
                    StrictRateCardPlanStatus.ACTIVE -> return true
                }
                return false
            }
            StrictRateCardPlanStatus.PENDING -> return next == StrictRateCardPlanStatus.CLOSED
            StrictRateCardPlanStatus.ACTIVE -> {
                when (next) {
                    StrictRateCardPlanStatus.CLOSED -> return true
                }
                return false
            }
            StrictRateCardPlanStatus.SUSPENDED -> return next == StrictRateCardPlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictRateCardPlanStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round(limit.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): StrictRateCardPlan {
        return StrictRateCardPlan(
            code = code,
            title = title,
            pallets = pallets,
            description = description,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict rate card plan in status draft. */
fun newStrictRateCardPlan(code: String, title: String): StrictRateCardPlan {
    return StrictRateCardPlan(
        code = code,
        title = title,
        pallets = 0,
        description = "",
        limit = 0,
        status = StrictRateCardPlanStatus.DRAFT,
    )
}
