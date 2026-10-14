package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted service plan milestone is in its life cycle. */
enum class WeightedServicePlanMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted service plan milestone as the service plan team keeps it. */
data class WeightedServicePlanMilestone(
    var code: String,
    var label: String,
    var routeCode: String,
    var weight: Double,
    var floorValue: Double,
    var externalRef: String,
    var distanceKm: Double,
    var status: WeightedServicePlanMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServicePlanMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServicePlanMilestoneStatus): Boolean {
        when (status) {
            WeightedServicePlanMilestoneStatus.REQUESTED -> {
                when (next) {
                    WeightedServicePlanMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            WeightedServicePlanMilestoneStatus.GRANTED -> return next == WeightedServicePlanMilestoneStatus.REVOKED
            WeightedServicePlanMilestoneStatus.REVOKED -> {
                when (next) {
                    WeightedServicePlanMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedServicePlanMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("weighted service plan milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted service plan milestone"} $code"

    fun effectiveExposure(): Double = round((floorValue - weight) * 100) / 100

    fun snapshot(): WeightedServicePlanMilestone {
        return WeightedServicePlanMilestone(
            code = code,
            label = label,
            routeCode = routeCode,
            weight = weight,
            floorValue = floorValue,
            externalRef = externalRef,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted service plan milestone in status requested. */
fun newWeightedServicePlanMilestone(code: String, label: String): WeightedServicePlanMilestone {
    return WeightedServicePlanMilestone(
        code = code,
        label = label,
        routeCode = "",
        weight = 0.0,
        floorValue = 0.0,
        externalRef = "",
        distanceKm = 0.0,
        status = WeightedServicePlanMilestoneStatus.REQUESTED,
    )
}
