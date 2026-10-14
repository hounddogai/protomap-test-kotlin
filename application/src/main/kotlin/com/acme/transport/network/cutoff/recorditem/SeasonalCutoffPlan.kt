package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal cutoff plan is in its life cycle. */
enum class SeasonalCutoffPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal cutoff plan and its current status. */
data class SeasonalCutoffPlan(
    var code: String,
    var name: String,
    var discount: Double,
    var windowDays: Int,
    var capacity: Int,
    var cost: Double,
    var phone: String,
    var amount: Double,
    var ratio: Double,
    var routeCode: String,
    var quantity: Int,
    var status: SeasonalCutoffPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCutoffPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCutoffPlanStatus): Boolean {
        when (status) {
            SeasonalCutoffPlanStatus.OPEN -> return next == SeasonalCutoffPlanStatus.REOPENED || next == SeasonalCutoffPlanStatus.ARCHIVED
            SeasonalCutoffPlanStatus.IN_REVIEW -> return next == SeasonalCutoffPlanStatus.REOPENED
            SeasonalCutoffPlanStatus.RESOLVED -> return next == SeasonalCutoffPlanStatus.REOPENED || next == SeasonalCutoffPlanStatus.ARCHIVED
            SeasonalCutoffPlanStatus.REOPENED -> {
                when (next) {
                    SeasonalCutoffPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCutoffPlanStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cutoff plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal cutoff plan"} $code"

    fun projectedExposure(): Double = round((amount - discount) * 100) / 100

    fun snapshot(): SeasonalCutoffPlan {
        return SeasonalCutoffPlan(
            code = code,
            name = name,
            discount = discount,
            windowDays = windowDays,
            capacity = capacity,
            cost = cost,
            phone = phone,
            amount = amount,
            ratio = ratio,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal cutoff plan in status open. */
fun newSeasonalCutoffPlan(code: String, name: String): SeasonalCutoffPlan {
    return SeasonalCutoffPlan(
        code = code,
        name = name,
        discount = 0.0,
        windowDays = 0,
        capacity = 0,
        cost = 0.0,
        phone = "",
        amount = 0.0,
        ratio = 0.0,
        routeCode = "",
        quantity = 0,
        status = SeasonalCutoffPlanStatus.OPEN,
    )
}
