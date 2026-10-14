package com.acme.transport.network.servicearea.window

import com.acme.transport.kit.text.normalize

// The service area window records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area window is in its life cycle. */
enum class ServiceAreaWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a service area window and its current status. */
data class ServiceAreaWindow(
    var code: String,
    var title: String,
    var timezone: String,
    var vehicleId: String,
    var source: String,
    var region: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var status: ServiceAreaWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaWindowStatus): Boolean {
        when (status) {
            ServiceAreaWindowStatus.PLANNED -> return next == ServiceAreaWindowStatus.IN_PROGRESS
            ServiceAreaWindowStatus.SCHEDULED -> return next == ServiceAreaWindowStatus.IN_PROGRESS || next == ServiceAreaWindowStatus.COMPLETED
            ServiceAreaWindowStatus.IN_PROGRESS -> return next == ServiceAreaWindowStatus.CANCELLED
            ServiceAreaWindowStatus.COMPLETED -> return next == ServiceAreaWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServiceAreaWindowStatus) {
        if (!canMoveTo(next)) {
            error("service area window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area window"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): ServiceAreaWindow {
        return ServiceAreaWindow(
            code = code,
            title = title,
            timezone = timezone,
            vehicleId = vehicleId,
            source = source,
            region = region,
            shiftCode = shiftCode,
            tags = tags,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a service area window in status planned. */
fun newServiceAreaWindow(code: String, title: String): ServiceAreaWindow {
    return ServiceAreaWindow(
        code = code,
        title = title,
        timezone = "",
        vehicleId = "",
        source = "",
        region = "",
        shiftCode = "",
        tags = mutableListOf<String>(),
        pallets = 0,
        status = ServiceAreaWindowStatus.PLANNED,
    )
}
