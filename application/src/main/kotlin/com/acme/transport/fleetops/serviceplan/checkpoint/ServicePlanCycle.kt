package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan cycle is in its life cycle. */
enum class ServicePlanCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a service plan cycle and its current status. */
data class ServicePlanCycle(
    var code: String,
    var title: String,
    var depot: String,
    var windowDays: Int,
    var reviewer: String,
    var durationMinutes: Int,
    var channel: String,
    var loadKg: Double,
    var status: ServicePlanCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanCycleStatus): Boolean {
        when (status) {
            ServicePlanCycleStatus.REQUESTED -> return next == ServicePlanCycleStatus.GRANTED || next == ServicePlanCycleStatus.REVOKED
            ServicePlanCycleStatus.GRANTED -> {
                when (next) {
                    ServicePlanCycleStatus.REVOKED, ServicePlanCycleStatus.EXPIRED -> return true
                }
                return false
            }
            ServicePlanCycleStatus.REVOKED -> return next == ServicePlanCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ServicePlanCycleStatus) {
        if (!canMoveTo(next)) {
            error("service plan cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service plan cycle"} $code"

    fun effectiveTotal(): Double = round((loadKg + windowDays.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanCycle {
        return ServicePlanCycle(
            code = code,
            title = title,
            depot = depot,
            windowDays = windowDays,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            channel = channel,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a service plan cycle in status requested. */
fun newServicePlanCycle(code: String, title: String): ServicePlanCycle {
    return ServicePlanCycle(
        code = code,
        title = title,
        depot = "",
        windowDays = 0,
        reviewer = "",
        durationMinutes = 0,
        channel = "",
        loadKg = 0.0,
        status = ServicePlanCycleStatus.REQUESTED,
    )
}
