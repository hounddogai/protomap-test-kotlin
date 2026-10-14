package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan group is in its life cycle. */
enum class ServicePlanGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A service plan group as the service plan team keeps it. */
data class ServicePlanGroup(
    var code: String,
    var name: String,
    var cost: Double,
    var description: String,
    var notes: String,
    var vehicleId: String,
    var timezone: String,
    var batchSize: Int,
    var status: ServicePlanGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanGroupStatus): Boolean {
        when (status) {
            ServicePlanGroupStatus.PLANNED -> {
                when (next) {
                    ServicePlanGroupStatus.IN_PROGRESS, ServicePlanGroupStatus.COMPLETED -> return true
                }
                return false
            }
            ServicePlanGroupStatus.SCHEDULED -> return next == ServicePlanGroupStatus.IN_PROGRESS || next == ServicePlanGroupStatus.CANCELLED
            ServicePlanGroupStatus.IN_PROGRESS -> {
                when (next) {
                    ServicePlanGroupStatus.COMPLETED, ServicePlanGroupStatus.CANCELLED -> return true
                }
                return false
            }
            ServicePlanGroupStatus.COMPLETED -> return next == ServicePlanGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServicePlanGroupStatus) {
        if (!canMoveTo(next)) {
            error("service plan group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round(batchSize.toDouble() * cost * 100) / 100

    fun snapshot(): ServicePlanGroup {
        return ServicePlanGroup(
            code = code,
            name = name,
            cost = cost,
            description = description,
            notes = notes,
            vehicleId = vehicleId,
            timezone = timezone,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan group in status planned. */
fun newServicePlanGroup(code: String, name: String): ServicePlanGroup {
    return ServicePlanGroup(
        code = code,
        name = name,
        cost = 0.0,
        description = "",
        notes = "",
        vehicleId = "",
        timezone = "",
        batchSize = 0,
        status = ServicePlanGroupStatus.PLANNED,
    )
}
