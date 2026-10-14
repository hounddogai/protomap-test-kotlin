package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident plan is in its life cycle. */
enum class IncidentPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A incident plan as the incident team keeps it. */
data class IncidentPlan(
    var code: String,
    var label: String,
    var vehicleId: String,
    var cost: Double,
    var ownerId: String,
    var trailerId: String,
    var country: String,
    var windowDays: Int,
    var startsAt: Int,
    var status: IncidentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentPlanStatus): Boolean {
        when (status) {
            IncidentPlanStatus.CREATED -> return next == IncidentPlanStatus.PUBLISHED || next == IncidentPlanStatus.RETIRED
            IncidentPlanStatus.VALIDATED -> return next == IncidentPlanStatus.RETIRED
            IncidentPlanStatus.PUBLISHED -> {
                when (next) {
                    IncidentPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentPlanStatus) {
        if (!canMoveTo(next)) {
            error("incident plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round((windowDays.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): IncidentPlan {
        return IncidentPlan(
            code = code,
            label = label,
            vehicleId = vehicleId,
            cost = cost,
            ownerId = ownerId,
            trailerId = trailerId,
            country = country,
            windowDays = windowDays,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a incident plan in status created. */
fun newIncidentPlan(code: String, label: String): IncidentPlan {
    return IncidentPlan(
        code = code,
        label = label,
        vehicleId = "",
        cost = 0.0,
        ownerId = "",
        trailerId = "",
        country = "",
        windowDays = 0,
        startsAt = 0,
        status = IncidentPlanStatus.CREATED,
    )
}
