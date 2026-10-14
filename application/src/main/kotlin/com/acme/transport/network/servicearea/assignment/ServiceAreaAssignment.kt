package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area assignment records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area assignment is in its life cycle. */
enum class ServiceAreaAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service area assignment. Its code is unique within a tenant. */
data class ServiceAreaAssignment(
    var code: String,
    var label: String,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var ownerId: String,
    var attempts: Int,
    var fuelLiters: Double,
    var status: ServiceAreaAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaAssignmentStatus): Boolean {
        when (status) {
            ServiceAreaAssignmentStatus.PROPOSED -> return next == ServiceAreaAssignmentStatus.APPROVED || next == ServiceAreaAssignmentStatus.REJECTED
            ServiceAreaAssignmentStatus.APPROVED -> {
                when (next) {
                    ServiceAreaAssignmentStatus.REJECTED, ServiceAreaAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServiceAreaAssignmentStatus.REJECTED -> return next == ServiceAreaAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ServiceAreaAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("service area assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service area assignment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((rate + fuelLiters) * 100) / 100

    fun snapshot(): ServiceAreaAssignment {
        return ServiceAreaAssignment(
            code = code,
            label = label,
            rate = rate,
            attributes = attributes,
            ceiling = ceiling,
            ownerId = ownerId,
            attempts = attempts,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service area assignment in status proposed. */
fun newServiceAreaAssignment(code: String, label: String): ServiceAreaAssignment {
    return ServiceAreaAssignment(
        code = code,
        label = label,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        ownerId = "",
        attempts = 0,
        fuelLiters = 0.0,
        status = ServiceAreaAssignmentStatus.PROPOSED,
    )
}
