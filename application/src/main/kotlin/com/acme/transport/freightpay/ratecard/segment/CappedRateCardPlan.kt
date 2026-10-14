package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rate card plan is in its life cycle. */
enum class CappedRateCardPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped rate card plan and its current status. */
data class CappedRateCardPlan(
    var code: String,
    var name: String,
    var score: Double,
    var durationMinutes: Int,
    var routeCode: String,
    var maximum: Double,
    var locale: String,
    var cost: Double,
    var etaMinutes: Int,
    var status: CappedRateCardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRateCardPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRateCardPlanStatus): Boolean {
        when (status) {
            CappedRateCardPlanStatus.CREATED -> return next == CappedRateCardPlanStatus.VALIDATED || next == CappedRateCardPlanStatus.PUBLISHED
            CappedRateCardPlanStatus.VALIDATED -> return next == CappedRateCardPlanStatus.PUBLISHED || next == CappedRateCardPlanStatus.RETIRED
            CappedRateCardPlanStatus.PUBLISHED -> return next == CappedRateCardPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedRateCardPlanStatus) {
        if (!canMoveTo(next)) {
            error("capped rate card plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((score - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedRateCardPlan {
        return CappedRateCardPlan(
            code = code,
            name = name,
            score = score,
            durationMinutes = durationMinutes,
            routeCode = routeCode,
            maximum = maximum,
            locale = locale,
            cost = cost,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped rate card plan in status created. */
fun newCappedRateCardPlan(code: String, name: String): CappedRateCardPlan {
    return CappedRateCardPlan(
        code = code,
        name = name,
        score = 0.0,
        durationMinutes = 0,
        routeCode = "",
        maximum = 0.0,
        locale = "",
        cost = 0.0,
        etaMinutes = 0,
        status = CappedRateCardPlanStatus.CREATED,
    )
}
