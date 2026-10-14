package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.normalize

/** Where a service area mapping is in its life cycle. */
enum class ServiceAreaMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service area mapping. Its code is unique within a tenant. */
data class ServiceAreaMapping(
    var code: String,
    var name: String,
    var maximum: Double,
    var checksum: String,
    var carrier: String,
    var vehicleId: String,
    var status: ServiceAreaMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaMappingStatus): Boolean {
        when (status) {
            ServiceAreaMappingStatus.NEW -> {
                when (next) {
                    ServiceAreaMappingStatus.DONE -> return true
                }
                return false
            }
            ServiceAreaMappingStatus.QUEUED -> return next == ServiceAreaMappingStatus.RUNNING || next == ServiceAreaMappingStatus.FAILED
            ServiceAreaMappingStatus.RUNNING -> {
                when (next) {
                    ServiceAreaMappingStatus.DONE -> return true
                }
                return false
            }
            ServiceAreaMappingStatus.DONE -> {
                when (next) {
                    ServiceAreaMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaMappingStatus) {
        if (!canMoveTo(next)) {
            error("service area mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = maximum

    fun snapshot(): ServiceAreaMapping {
        return ServiceAreaMapping(
            code = code,
            name = name,
            maximum = maximum,
            checksum = checksum,
            carrier = carrier,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area mapping in status new. */
fun newServiceAreaMapping(code: String, name: String): ServiceAreaMapping {
    return ServiceAreaMapping(
        code = code,
        name = name,
        maximum = 0.0,
        checksum = "",
        carrier = "",
        vehicleId = "",
        status = ServiceAreaMappingStatus.NEW,
    )
}
