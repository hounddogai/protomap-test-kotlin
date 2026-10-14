package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive rate card plan is in its life cycle. */
enum class AdaptiveRateCardPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive rate card plan. Its code is unique within a tenant. */
data class AdaptiveRateCardPlan(
    var code: String,
    var name: String,
    var score: Double,
    var retries: Int,
    var title: String,
    var pallets: Int,
    var status: AdaptiveRateCardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRateCardPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRateCardPlanStatus): Boolean {
        when (status) {
            AdaptiveRateCardPlanStatus.IDLE -> return next == AdaptiveRateCardPlanStatus.READY || next == AdaptiveRateCardPlanStatus.STOPPED
            AdaptiveRateCardPlanStatus.WARMING -> return next == AdaptiveRateCardPlanStatus.DRAINING || next == AdaptiveRateCardPlanStatus.STOPPED
            AdaptiveRateCardPlanStatus.READY -> {
                when (next) {
                    AdaptiveRateCardPlanStatus.DRAINING, AdaptiveRateCardPlanStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveRateCardPlanStatus.DRAINING -> return next == AdaptiveRateCardPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveRateCardPlanStatus) {
        if (!canMoveTo(next)) {
            error("adaptive rate card plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round((retries.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRateCardPlan {
        return AdaptiveRateCardPlan(
            code = code,
            name = name,
            score = score,
            retries = retries,
            title = title,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive rate card plan in status idle. */
fun newAdaptiveRateCardPlan(code: String, name: String): AdaptiveRateCardPlan {
    return AdaptiveRateCardPlan(
        code = code,
        name = name,
        score = 0.0,
        retries = 0,
        title = "",
        pallets = 0,
        status = AdaptiveRateCardPlanStatus.IDLE,
    )
}
