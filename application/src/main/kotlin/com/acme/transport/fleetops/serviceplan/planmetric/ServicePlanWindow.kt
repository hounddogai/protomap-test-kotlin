package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.text.normalize

/** Where a service plan window is in its life cycle. */
enum class ServicePlanWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service plan window as the service plan team keeps it. */
data class ServicePlanWindow(
    var code: String,
    var name: String,
    var odometer: Int,
    var carrier: String,
    var summary: String,
    var status: ServicePlanWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanWindowStatus): Boolean {
        when (status) {
            ServicePlanWindowStatus.PROPOSED -> {
                when (next) {
                    ServicePlanWindowStatus.APPROVED, ServicePlanWindowStatus.REJECTED -> return true
                }
                return false
            }
            ServicePlanWindowStatus.APPROVED -> return next == ServicePlanWindowStatus.REJECTED || next == ServicePlanWindowStatus.WITHDRAWN
            ServicePlanWindowStatus.REJECTED -> return next == ServicePlanWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ServicePlanWindowStatus) {
        if (!canMoveTo(next)) {
            error("service plan window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): ServicePlanWindow {
        return ServicePlanWindow(
            code = code,
            name = name,
            odometer = odometer,
            carrier = carrier,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service plan window in status proposed. */
fun newServicePlanWindow(code: String, name: String): ServicePlanWindow {
    return ServicePlanWindow(
        code = code,
        name = name,
        odometer = 0,
        carrier = "",
        summary = "",
        status = ServicePlanWindowStatus.PROPOSED,
    )
}
