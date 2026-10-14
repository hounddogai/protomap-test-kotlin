package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card plan is in its life cycle. */
enum class RateCardPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rate card plan. Its code is unique within a tenant. */
data class RateCardPlan(
    var code: String,
    var title: String,
    var unit: String,
    var cost: Double,
    var depot: String,
    var contactEmail: String,
    var capacity: Int,
    var version: Int,
    var segment: String,
    var channel: String,
    var status: RateCardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardPlanStatus): Boolean {
        when (status) {
            RateCardPlanStatus.PLANNED -> {
                when (next) {
                    RateCardPlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RateCardPlanStatus.SCHEDULED -> return next == RateCardPlanStatus.IN_PROGRESS || next == RateCardPlanStatus.COMPLETED
            RateCardPlanStatus.IN_PROGRESS -> {
                when (next) {
                    RateCardPlanStatus.CANCELLED -> return true
                }
                return false
            }
            RateCardPlanStatus.COMPLETED -> return next == RateCardPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RateCardPlanStatus) {
        if (!canMoveTo(next)) {
            error("rate card plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"rate card plan"} $code"

    fun projectedValue(): Double = round(version.toDouble() * cost * 100) / 100

    fun snapshot(): RateCardPlan {
        return RateCardPlan(
            code = code,
            title = title,
            unit = unit,
            cost = cost,
            depot = depot,
            contactEmail = contactEmail,
            capacity = capacity,
            version = version,
            segment = segment,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rate card plan in status planned. */
fun newRateCardPlan(code: String, title: String): RateCardPlan {
    return RateCardPlan(
        code = code,
        title = title,
        unit = "",
        cost = 0.0,
        depot = "",
        contactEmail = "",
        capacity = 0,
        version = 0,
        segment = "",
        channel = "",
        status = RateCardPlanStatus.PLANNED,
    )
}
