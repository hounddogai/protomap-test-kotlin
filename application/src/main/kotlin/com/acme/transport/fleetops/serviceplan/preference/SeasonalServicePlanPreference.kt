package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal service plan preference records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a seasonal service plan preference is in its life cycle. */
enum class SeasonalServicePlanPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a seasonal service plan preference and its current status. */
data class SeasonalServicePlanPreference(
    var code: String,
    var label: String,
    var routeCode: String,
    var windowDays: Int,
    var minimum: Double,
    var status: SeasonalServicePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServicePlanPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServicePlanPreferenceStatus): Boolean {
        when (status) {
            SeasonalServicePlanPreferenceStatus.PROPOSED -> return next == SeasonalServicePlanPreferenceStatus.WITHDRAWN
            SeasonalServicePlanPreferenceStatus.APPROVED -> return next == SeasonalServicePlanPreferenceStatus.REJECTED || next == SeasonalServicePlanPreferenceStatus.WITHDRAWN
            SeasonalServicePlanPreferenceStatus.REJECTED -> {
                when (next) {
                    SeasonalServicePlanPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServicePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((windowDays.toDouble() + minimum) * 100) / 100

    fun snapshot(): SeasonalServicePlanPreference {
        return SeasonalServicePlanPreference(
            code = code,
            label = label,
            routeCode = routeCode,
            windowDays = windowDays,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal service plan preference in status proposed. */
fun newSeasonalServicePlanPreference(code: String, label: String): SeasonalServicePlanPreference {
    return SeasonalServicePlanPreference(
        code = code,
        label = label,
        routeCode = "",
        windowDays = 0,
        minimum = 0.0,
        status = SeasonalServicePlanPreferenceStatus.PROPOSED,
    )
}
