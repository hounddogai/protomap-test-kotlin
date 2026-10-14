package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset rule is in its life cycle. */
enum class FallbackRouteOffsetRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback route offset rule, with what the planners need to act on it. */
data class FallbackRouteOffsetRule(
    var code: String,
    var title: String,
    var windowDays: Int,
    var distanceKm: Double,
    var unit: String,
    var approved: Boolean,
    var label: String,
    var status: FallbackRouteOffsetRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetRuleStatus): Boolean {
        when (status) {
            FallbackRouteOffsetRuleStatus.OPEN -> return next == FallbackRouteOffsetRuleStatus.IN_REVIEW || next == FallbackRouteOffsetRuleStatus.ARCHIVED
            FallbackRouteOffsetRuleStatus.IN_REVIEW -> {
                when (next) {
                    FallbackRouteOffsetRuleStatus.REOPENED, FallbackRouteOffsetRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRouteOffsetRuleStatus.RESOLVED -> return next == FallbackRouteOffsetRuleStatus.REOPENED
            FallbackRouteOffsetRuleStatus.REOPENED -> return next == FallbackRouteOffsetRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetRuleStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun weightedTotal(): Double = round((distanceKm + windowDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackRouteOffsetRule {
        return FallbackRouteOffsetRule(
            code = code,
            title = title,
            windowDays = windowDays,
            distanceKm = distanceKm,
            unit = unit,
            approved = approved,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback route offset rule in status open. */
fun newFallbackRouteOffsetRule(code: String, title: String): FallbackRouteOffsetRule {
    return FallbackRouteOffsetRule(
        code = code,
        title = title,
        windowDays = 0,
        distanceKm = 0.0,
        unit = "",
        approved = false,
        label = "",
        status = FallbackRouteOffsetRuleStatus.OPEN,
    )
}
