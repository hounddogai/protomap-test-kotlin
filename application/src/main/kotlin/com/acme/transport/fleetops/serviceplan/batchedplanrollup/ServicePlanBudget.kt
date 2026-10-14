package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan budget is in its life cycle. */
enum class ServicePlanBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a service plan budget and its current status. */
data class ServicePlanBudget(
    var code: String,
    var label: String,
    var sequence: Int,
    var stopCount: Int,
    var margin: Double,
    var startsAt: Int,
    var checksum: String,
    var description: String,
    var trailerId: String,
    var timezone: String,
    var status: ServicePlanBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanBudgetStatus): Boolean {
        when (status) {
            ServicePlanBudgetStatus.IDLE -> return next == ServicePlanBudgetStatus.READY || next == ServicePlanBudgetStatus.DRAINING
            ServicePlanBudgetStatus.WARMING -> return next == ServicePlanBudgetStatus.DRAINING || next == ServicePlanBudgetStatus.STOPPED
            ServicePlanBudgetStatus.READY -> {
                when (next) {
                    ServicePlanBudgetStatus.DRAINING, ServicePlanBudgetStatus.STOPPED -> return true
                }
                return false
            }
            ServicePlanBudgetStatus.DRAINING -> {
                when (next) {
                    ServicePlanBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanBudgetStatus) {
        if (!canMoveTo(next)) {
            error("service plan budget $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round(margin * startsAt.toDouble() * 100) / 100

    fun snapshot(): ServicePlanBudget {
        return ServicePlanBudget(
            code = code,
            label = label,
            sequence = sequence,
            stopCount = stopCount,
            margin = margin,
            startsAt = startsAt,
            checksum = checksum,
            description = description,
            trailerId = trailerId,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service plan budget in status idle. */
fun newServicePlanBudget(code: String, label: String): ServicePlanBudget {
    return ServicePlanBudget(
        code = code,
        label = label,
        sequence = 0,
        stopCount = 0,
        margin = 0.0,
        startsAt = 0,
        checksum = "",
        description = "",
        trailerId = "",
        timezone = "",
        status = ServicePlanBudgetStatus.IDLE,
    )
}
