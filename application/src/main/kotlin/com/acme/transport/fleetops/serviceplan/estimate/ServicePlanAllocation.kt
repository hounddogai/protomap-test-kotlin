package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan allocation is in its life cycle. */
enum class ServicePlanAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One service plan allocation, with what the planners need to act on it. */
data class ServicePlanAllocation(
    var code: String,
    var label: String,
    var ceiling: Double,
    var quantity: Int,
    var timezone: String,
    var externalRef: String,
    var budget: Double,
    var batchSize: Int,
    var maximum: Double,
    var limit: Int,
    var status: ServicePlanAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanAllocationStatus): Boolean {
        when (status) {
            ServicePlanAllocationStatus.PLANNED -> {
                when (next) {
                    ServicePlanAllocationStatus.SCHEDULED -> return true
                }
                return false
            }
            ServicePlanAllocationStatus.SCHEDULED -> return next == ServicePlanAllocationStatus.CANCELLED
            ServicePlanAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    ServicePlanAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            ServicePlanAllocationStatus.COMPLETED -> {
                when (next) {
                    ServicePlanAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanAllocationStatus) {
        if (!canMoveTo(next)) {
            error("service plan allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service plan allocation"} $code"

    fun projectedExposure(): Double = round((limit.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): ServicePlanAllocation {
        return ServicePlanAllocation(
            code = code,
            label = label,
            ceiling = ceiling,
            quantity = quantity,
            timezone = timezone,
            externalRef = externalRef,
            budget = budget,
            batchSize = batchSize,
            maximum = maximum,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service plan allocation in status planned. */
fun newServicePlanAllocation(code: String, label: String): ServicePlanAllocation {
    return ServicePlanAllocation(
        code = code,
        label = label,
        ceiling = 0.0,
        quantity = 0,
        timezone = "",
        externalRef = "",
        budget = 0.0,
        batchSize = 0,
        maximum = 0.0,
        limit = 0,
        status = ServicePlanAllocationStatus.PLANNED,
    )
}
