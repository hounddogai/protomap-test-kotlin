package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive incident plan records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a adaptive incident plan is in its life cycle. */
enum class AdaptiveIncidentPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a adaptive incident plan and its current status. */
data class AdaptiveIncidentPlan(
    var code: String,
    var label: String,
    var capacity: Int,
    var channel: String,
    var depot: String,
    var pinned: Boolean,
    var graceDays: Int,
    var lane: String,
    var status: AdaptiveIncidentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIncidentPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIncidentPlanStatus): Boolean {
        when (status) {
            AdaptiveIncidentPlanStatus.CREATED -> return next == AdaptiveIncidentPlanStatus.VALIDATED
            AdaptiveIncidentPlanStatus.VALIDATED -> {
                when (next) {
                    AdaptiveIncidentPlanStatus.PUBLISHED, AdaptiveIncidentPlanStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveIncidentPlanStatus.PUBLISHED -> return next == AdaptiveIncidentPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveIncidentPlanStatus) {
        if (!canMoveTo(next)) {
            error("adaptive incident plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round(capacity.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveIncidentPlan {
        return AdaptiveIncidentPlan(
            code = code,
            label = label,
            capacity = capacity,
            channel = channel,
            depot = depot,
            pinned = pinned,
            graceDays = graceDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive incident plan in status created. */
fun newAdaptiveIncidentPlan(code: String, label: String): AdaptiveIncidentPlan {
    return AdaptiveIncidentPlan(
        code = code,
        label = label,
        capacity = 0,
        channel = "",
        depot = "",
        pinned = false,
        graceDays = 0,
        lane = "",
        status = AdaptiveIncidentPlanStatus.CREATED,
    )
}
