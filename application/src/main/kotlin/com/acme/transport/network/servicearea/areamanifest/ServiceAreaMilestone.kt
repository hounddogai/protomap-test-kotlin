package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area milestone is in its life cycle. */
enum class ServiceAreaMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a service area milestone and its current status. */
data class ServiceAreaMilestone(
    var code: String,
    var title: String,
    var cost: Double,
    var budget: Double,
    var vehicleId: String,
    var status: ServiceAreaMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaMilestoneStatus): Boolean {
        when (status) {
            ServiceAreaMilestoneStatus.CREATED -> return next == ServiceAreaMilestoneStatus.VALIDATED || next == ServiceAreaMilestoneStatus.PUBLISHED
            ServiceAreaMilestoneStatus.VALIDATED -> {
                when (next) {
                    ServiceAreaMilestoneStatus.PUBLISHED, ServiceAreaMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            ServiceAreaMilestoneStatus.PUBLISHED -> return next == ServiceAreaMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ServiceAreaMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("service area milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round((cost - budget) * 100) / 100

    fun snapshot(): ServiceAreaMilestone {
        return ServiceAreaMilestone(
            code = code,
            title = title,
            cost = cost,
            budget = budget,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a service area milestone in status created. */
fun newServiceAreaMilestone(code: String, title: String): ServiceAreaMilestone {
    return ServiceAreaMilestone(
        code = code,
        title = title,
        cost = 0.0,
        budget = 0.0,
        vehicleId = "",
        status = ServiceAreaMilestoneStatus.CREATED,
    )
}
