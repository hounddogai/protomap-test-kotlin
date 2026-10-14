package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize

// The service area request records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area request is in its life cycle. */
enum class ServiceAreaRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service area request. Its code is unique within a tenant. */
data class ServiceAreaRequest(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var approved: Boolean,
    var lane: String,
    var status: ServiceAreaRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaRequestStatus): Boolean {
        when (status) {
            ServiceAreaRequestStatus.IDLE -> return next == ServiceAreaRequestStatus.READY
            ServiceAreaRequestStatus.WARMING -> {
                when (next) {
                    ServiceAreaRequestStatus.READY, ServiceAreaRequestStatus.DRAINING -> return true
                }
                return false
            }
            ServiceAreaRequestStatus.READY -> return next == ServiceAreaRequestStatus.DRAINING || next == ServiceAreaRequestStatus.STOPPED
            ServiceAreaRequestStatus.DRAINING -> return next == ServiceAreaRequestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServiceAreaRequestStatus) {
        if (!canMoveTo(next)) {
            error("service area request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): ServiceAreaRequest {
        return ServiceAreaRequest(
            code = code,
            label = label,
            distanceKm = distanceKm,
            approved = approved,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service area request in status idle. */
fun newServiceAreaRequest(code: String, label: String): ServiceAreaRequest {
    return ServiceAreaRequest(
        code = code,
        label = label,
        distanceKm = 0.0,
        approved = false,
        lane = "",
        status = ServiceAreaRequestStatus.IDLE,
    )
}
