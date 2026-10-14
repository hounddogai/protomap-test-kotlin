package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global availability allocation is in its life cycle. */
enum class GlobalAvailabilityAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global availability allocation. Its code is unique within a tenant. */
data class GlobalAvailabilityAllocation(
    var code: String,
    var name: String,
    var maximum: Double,
    var ownerId: String,
    var weight: Double,
    var externalRef: String,
    var trailerId: String,
    var phone: String,
    var batchSize: Int,
    var vehicleId: String,
    var status: GlobalAvailabilityAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAvailabilityAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAvailabilityAllocationStatus): Boolean {
        when (status) {
            GlobalAvailabilityAllocationStatus.PLANNED -> {
                when (next) {
                    GlobalAvailabilityAllocationStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalAvailabilityAllocationStatus.SCHEDULED -> return next == GlobalAvailabilityAllocationStatus.COMPLETED
            GlobalAvailabilityAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalAvailabilityAllocationStatus.COMPLETED, GlobalAvailabilityAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalAvailabilityAllocationStatus.COMPLETED -> return next == GlobalAvailabilityAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalAvailabilityAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global availability allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((maximum - batchSize.toDouble()) * 100) / 100

    fun snapshot(): GlobalAvailabilityAllocation {
        return GlobalAvailabilityAllocation(
            code = code,
            name = name,
            maximum = maximum,
            ownerId = ownerId,
            weight = weight,
            externalRef = externalRef,
            trailerId = trailerId,
            phone = phone,
            batchSize = batchSize,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global availability allocation in status planned. */
fun newGlobalAvailabilityAllocation(code: String, name: String): GlobalAvailabilityAllocation {
    return GlobalAvailabilityAllocation(
        code = code,
        name = name,
        maximum = 0.0,
        ownerId = "",
        weight = 0.0,
        externalRef = "",
        trailerId = "",
        phone = "",
        batchSize = 0,
        vehicleId = "",
        status = GlobalAvailabilityAllocationStatus.PLANNED,
    )
}
