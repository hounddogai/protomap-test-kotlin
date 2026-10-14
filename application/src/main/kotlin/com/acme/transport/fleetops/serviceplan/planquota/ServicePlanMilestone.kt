package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan milestone is in its life cycle. */
enum class ServicePlanMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service plan milestone as the service plan team keeps it. */
data class ServicePlanMilestone(
    var code: String,
    var title: String,
    var createdAt: Int,
    var pinned: Boolean,
    var distanceKm: Double,
    var visible: Boolean,
    var amount: Double,
    var weight: Double,
    var priority: Int,
    var locale: String,
    var status: ServicePlanMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanMilestoneStatus): Boolean {
        when (status) {
            ServicePlanMilestoneStatus.IDLE -> return next == ServicePlanMilestoneStatus.DRAINING
            ServicePlanMilestoneStatus.WARMING -> return next == ServicePlanMilestoneStatus.DRAINING
            ServicePlanMilestoneStatus.READY -> return next == ServicePlanMilestoneStatus.DRAINING
            ServicePlanMilestoneStatus.DRAINING -> {
                when (next) {
                    ServicePlanMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("service plan milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((weight + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanMilestone {
        return ServicePlanMilestone(
            code = code,
            title = title,
            createdAt = createdAt,
            pinned = pinned,
            distanceKm = distanceKm,
            visible = visible,
            amount = amount,
            weight = weight,
            priority = priority,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a service plan milestone in status idle. */
fun newServicePlanMilestone(code: String, title: String): ServicePlanMilestone {
    return ServicePlanMilestone(
        code = code,
        title = title,
        createdAt = 0,
        pinned = false,
        distanceKm = 0.0,
        visible = false,
        amount = 0.0,
        weight = 0.0,
        priority = 0,
        locale = "",
        status = ServicePlanMilestoneStatus.IDLE,
    )
}
