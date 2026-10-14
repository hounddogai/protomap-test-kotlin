package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan baseline is in its life cycle. */
enum class ServicePlanBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service plan baseline. Its code is unique within a tenant. */
data class ServicePlanBaseline(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var lane: String,
    var contactEmail: String,
    var cost: Double,
    var status: ServicePlanBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanBaselineStatus): Boolean {
        when (status) {
            ServicePlanBaselineStatus.CREATED -> return next == ServicePlanBaselineStatus.VALIDATED
            ServicePlanBaselineStatus.VALIDATED -> {
                when (next) {
                    ServicePlanBaselineStatus.RETIRED -> return true
                }
                return false
            }
            ServicePlanBaselineStatus.PUBLISHED -> return next == ServicePlanBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanBaselineStatus) {
        if (!canMoveTo(next)) {
            error("service plan baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service plan baseline"} $code"

    fun netLoad(): Double = round(cost * distanceKm * 100) / 100

    fun snapshot(): ServicePlanBaseline {
        return ServicePlanBaseline(
            code = code,
            label = label,
            distanceKm = distanceKm,
            lane = lane,
            contactEmail = contactEmail,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service plan baseline in status created. */
fun newServicePlanBaseline(code: String, label: String): ServicePlanBaseline {
    return ServicePlanBaseline(
        code = code,
        label = label,
        distanceKm = 0.0,
        lane = "",
        contactEmail = "",
        cost = 0.0,
        status = ServicePlanBaselineStatus.CREATED,
    )
}
