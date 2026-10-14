package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.text.normalize

/** Where a scheduled parcel allocation is in its life cycle. */
enum class ScheduledParcelAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled parcel allocation. Its code is unique within a tenant. */
data class ScheduledParcelAllocation(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var driverId: String,
    var routeCode: String,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var status: ScheduledParcelAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledParcelAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledParcelAllocationStatus): Boolean {
        when (status) {
            ScheduledParcelAllocationStatus.DRAFT -> return next == ScheduledParcelAllocationStatus.SUSPENDED
            ScheduledParcelAllocationStatus.PENDING -> return next == ScheduledParcelAllocationStatus.CLOSED
            ScheduledParcelAllocationStatus.ACTIVE -> {
                when (next) {
                    ScheduledParcelAllocationStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledParcelAllocationStatus.SUSPENDED -> return next == ScheduledParcelAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledParcelAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled parcel allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): ScheduledParcelAllocation {
        return ScheduledParcelAllocation(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            driverId = driverId,
            routeCode = routeCode,
            locale = locale,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled parcel allocation in status draft. */
fun newScheduledParcelAllocation(code: String, title: String): ScheduledParcelAllocation {
    return ScheduledParcelAllocation(
        code = code,
        title = title,
        fuelLiters = 0.0,
        driverId = "",
        routeCode = "",
        locale = "",
        attributes = mutableMapOf<String, String>(),
        status = ScheduledParcelAllocationStatus.DRAFT,
    )
}
