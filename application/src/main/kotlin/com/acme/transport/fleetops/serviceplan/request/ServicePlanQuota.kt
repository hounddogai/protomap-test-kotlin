package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan quota is in its life cycle. */
enum class ServicePlanQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One service plan quota, with what the planners need to act on it. */
data class ServicePlanQuota(
    var code: String,
    var label: String,
    var version: Int,
    var source: String,
    var trailerId: String,
    var approved: Boolean,
    var unit: String,
    var startsAt: Int,
    var durationMinutes: Int,
    var reviewer: String,
    var status: ServicePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanQuotaStatus): Boolean {
        when (status) {
            ServicePlanQuotaStatus.PLANNED -> {
                when (next) {
                    ServicePlanQuotaStatus.SCHEDULED -> return true
                }
                return false
            }
            ServicePlanQuotaStatus.SCHEDULED -> return next == ServicePlanQuotaStatus.COMPLETED
            ServicePlanQuotaStatus.IN_PROGRESS -> return next == ServicePlanQuotaStatus.COMPLETED || next == ServicePlanQuotaStatus.CANCELLED
            ServicePlanQuotaStatus.COMPLETED -> return next == ServicePlanQuotaStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServicePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("service plan quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round(startsAt.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): ServicePlanQuota {
        return ServicePlanQuota(
            code = code,
            label = label,
            version = version,
            source = source,
            trailerId = trailerId,
            approved = approved,
            unit = unit,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a service plan quota in status planned. */
fun newServicePlanQuota(code: String, label: String): ServicePlanQuota {
    return ServicePlanQuota(
        code = code,
        label = label,
        version = 0,
        source = "",
        trailerId = "",
        approved = false,
        unit = "",
        startsAt = 0,
        durationMinutes = 0,
        reviewer = "",
        status = ServicePlanQuotaStatus.PLANNED,
    )
}
