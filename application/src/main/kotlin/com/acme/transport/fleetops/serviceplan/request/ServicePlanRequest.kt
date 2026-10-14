package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service plan request records of fleetops's service plan work, and the rules, stores, and calculations
// around them.

/** Where a service plan request is in its life cycle. */
enum class ServicePlanRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a service plan request and its current status. */
data class ServicePlanRequest(
    var code: String,
    var label: String,
    var description: String,
    var checksum: String,
    var rate: Double,
    var minimum: Double,
    var odometer: Int,
    var source: String,
    var externalRef: String,
    var status: ServicePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanRequestStatus): Boolean {
        when (status) {
            ServicePlanRequestStatus.PROPOSED -> return next == ServicePlanRequestStatus.APPROVED || next == ServicePlanRequestStatus.REJECTED
            ServicePlanRequestStatus.APPROVED -> return next == ServicePlanRequestStatus.REJECTED || next == ServicePlanRequestStatus.WITHDRAWN
            ServicePlanRequestStatus.REJECTED -> {
                when (next) {
                    ServicePlanRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("service plan request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service plan request"} $code"

    fun netLoad(): Double = round(rate * odometer.toDouble() * 100) / 100

    fun snapshot(): ServicePlanRequest {
        return ServicePlanRequest(
            code = code,
            label = label,
            description = description,
            checksum = checksum,
            rate = rate,
            minimum = minimum,
            odometer = odometer,
            source = source,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a service plan request in status proposed. */
fun newServicePlanRequest(code: String, label: String): ServicePlanRequest {
    return ServicePlanRequest(
        code = code,
        label = label,
        description = "",
        checksum = "",
        rate = 0.0,
        minimum = 0.0,
        odometer = 0,
        source = "",
        externalRef = "",
        status = ServicePlanRequestStatus.PROPOSED,
    )
}
