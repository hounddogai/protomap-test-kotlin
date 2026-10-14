package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.text.normalize

/** Where a service plan mapping is in its life cycle. */
enum class ServicePlanMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a service plan mapping and its current status. */
data class ServicePlanMapping(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var minimum: Double,
    var carrier: String,
    var status: ServicePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanMappingStatus): Boolean {
        when (status) {
            ServicePlanMappingStatus.IDLE -> {
                when (next) {
                    ServicePlanMappingStatus.READY -> return true
                }
                return false
            }
            ServicePlanMappingStatus.WARMING -> return next == ServicePlanMappingStatus.READY
            ServicePlanMappingStatus.READY -> return next == ServicePlanMappingStatus.STOPPED
            ServicePlanMappingStatus.DRAINING -> return next == ServicePlanMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServicePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("service plan mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service plan mapping"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = minimum

    fun snapshot(): ServicePlanMapping {
        return ServicePlanMapping(
            code = code,
            label = label,
            attributes = attributes,
            unit = unit,
            minimum = minimum,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a service plan mapping in status idle. */
fun newServicePlanMapping(code: String, label: String): ServicePlanMapping {
    return ServicePlanMapping(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        minimum = 0.0,
        carrier = "",
        status = ServicePlanMappingStatus.IDLE,
    )
}
