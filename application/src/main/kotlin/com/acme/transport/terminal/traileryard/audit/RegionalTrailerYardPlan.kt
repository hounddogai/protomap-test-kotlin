package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional trailer yard plan is in its life cycle. */
enum class RegionalTrailerYardPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional trailer yard plan, with what the planners need to act on it. */
data class RegionalTrailerYardPlan(
    var code: String,
    var title: String,
    var locale: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var routeCode: String,
    var maximum: Double,
    var status: RegionalTrailerYardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardPlanStatus): Boolean {
        when (status) {
            RegionalTrailerYardPlanStatus.OPEN -> return next == RegionalTrailerYardPlanStatus.RESOLVED || next == RegionalTrailerYardPlanStatus.REOPENED
            RegionalTrailerYardPlanStatus.IN_REVIEW -> {
                when (next) {
                    RegionalTrailerYardPlanStatus.REOPENED -> return true
                }
                return false
            }
            RegionalTrailerYardPlanStatus.RESOLVED -> return next == RegionalTrailerYardPlanStatus.REOPENED || next == RegionalTrailerYardPlanStatus.ARCHIVED
            RegionalTrailerYardPlanStatus.REOPENED -> return next == RegionalTrailerYardPlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional trailer yard plan"} $code"

    fun weightedValue(): Double = round((etaMinutes.toDouble() - maximum) * 100) / 100

    fun snapshot(): RegionalTrailerYardPlan {
        return RegionalTrailerYardPlan(
            code = code,
            title = title,
            locale = locale,
            pinned = pinned,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional trailer yard plan in status open. */
fun newRegionalTrailerYardPlan(code: String, title: String): RegionalTrailerYardPlan {
    return RegionalTrailerYardPlan(
        code = code,
        title = title,
        locale = "",
        pinned = false,
        etaMinutes = 0,
        routeCode = "",
        maximum = 0.0,
        status = RegionalTrailerYardPlanStatus.OPEN,
    )
}
