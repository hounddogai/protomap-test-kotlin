package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal accessorial plan records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a seasonal accessorial plan is in its life cycle. */
enum class SeasonalAccessorialPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a seasonal accessorial plan and its current status. */
data class SeasonalAccessorialPlan(
    var code: String,
    var label: String,
    var source: String,
    var reviewer: String,
    var graceDays: Int,
    var limit: Int,
    var maximum: Double,
    var revision: Int,
    var status: SeasonalAccessorialPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAccessorialPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAccessorialPlanStatus): Boolean {
        when (status) {
            SeasonalAccessorialPlanStatus.IDLE -> return next == SeasonalAccessorialPlanStatus.WARMING || next == SeasonalAccessorialPlanStatus.DRAINING
            SeasonalAccessorialPlanStatus.WARMING -> return next == SeasonalAccessorialPlanStatus.DRAINING
            SeasonalAccessorialPlanStatus.READY -> {
                when (next) {
                    SeasonalAccessorialPlanStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalAccessorialPlanStatus.DRAINING -> {
                when (next) {
                    SeasonalAccessorialPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAccessorialPlanStatus) {
        if (!canMoveTo(next)) {
            error("seasonal accessorial plan $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"seasonal accessorial plan"} $code"

    fun effectiveLoad(): Double = round((maximum - graceDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalAccessorialPlan {
        return SeasonalAccessorialPlan(
            code = code,
            label = label,
            source = source,
            reviewer = reviewer,
            graceDays = graceDays,
            limit = limit,
            maximum = maximum,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal accessorial plan in status idle. */
fun newSeasonalAccessorialPlan(code: String, label: String): SeasonalAccessorialPlan {
    return SeasonalAccessorialPlan(
        code = code,
        label = label,
        source = "",
        reviewer = "",
        graceDays = 0,
        limit = 0,
        maximum = 0.0,
        revision = 0,
        status = SeasonalAccessorialPlanStatus.IDLE,
    )
}
