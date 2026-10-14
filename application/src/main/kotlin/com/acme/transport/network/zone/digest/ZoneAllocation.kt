package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.normalize

/** Where a zone allocation is in its life cycle. */
enum class ZoneAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A zone allocation. Its code is unique within a tenant. */
data class ZoneAllocation(
    var code: String,
    var title: String,
    var vehicleId: String,
    var distanceKm: Double,
    var externalRef: String,
    var dock: String,
    var carrier: String,
    var status: ZoneAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneAllocationStatus): Boolean {
        when (status) {
            ZoneAllocationStatus.PLANNED -> {
                when (next) {
                    ZoneAllocationStatus.SCHEDULED -> return true
                }
                return false
            }
            ZoneAllocationStatus.SCHEDULED -> return next == ZoneAllocationStatus.IN_PROGRESS || next == ZoneAllocationStatus.COMPLETED
            ZoneAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    ZoneAllocationStatus.COMPLETED, ZoneAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            ZoneAllocationStatus.COMPLETED -> return next == ZoneAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ZoneAllocationStatus) {
        if (!canMoveTo(next)) {
            error("zone allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"zone allocation"} $code"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): ZoneAllocation {
        return ZoneAllocation(
            code = code,
            title = title,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            externalRef = externalRef,
            dock = dock,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a zone allocation in status planned. */
fun newZoneAllocation(code: String, title: String): ZoneAllocation {
    return ZoneAllocation(
        code = code,
        title = title,
        vehicleId = "",
        distanceKm = 0.0,
        externalRef = "",
        dock = "",
        carrier = "",
        status = ZoneAllocationStatus.PLANNED,
    )
}
