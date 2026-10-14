package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area override is in its life cycle. */
enum class ServiceAreaOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One service area override, with what the planners need to act on it. */
data class ServiceAreaOverride(
    var code: String,
    var title: String,
    var graceDays: Int,
    var segment: String,
    var notes: String,
    var driverId: String,
    var budget: Double,
    var status: ServiceAreaOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaOverrideStatus): Boolean {
        when (status) {
            ServiceAreaOverrideStatus.PLANNED -> return next == ServiceAreaOverrideStatus.IN_PROGRESS || next == ServiceAreaOverrideStatus.COMPLETED
            ServiceAreaOverrideStatus.SCHEDULED -> {
                when (next) {
                    ServiceAreaOverrideStatus.COMPLETED, ServiceAreaOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            ServiceAreaOverrideStatus.IN_PROGRESS -> return next == ServiceAreaOverrideStatus.COMPLETED
            ServiceAreaOverrideStatus.COMPLETED -> return next == ServiceAreaOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ServiceAreaOverrideStatus) {
        if (!canMoveTo(next)) {
            error("service area override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area override"} $code"

    fun effectiveVolume(): Double = round((budget - graceDays.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaOverride {
        return ServiceAreaOverride(
            code = code,
            title = title,
            graceDays = graceDays,
            segment = segment,
            notes = notes,
            driverId = driverId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a service area override in status planned. */
fun newServiceAreaOverride(code: String, title: String): ServiceAreaOverride {
    return ServiceAreaOverride(
        code = code,
        title = title,
        graceDays = 0,
        segment = "",
        notes = "",
        driverId = "",
        budget = 0.0,
        status = ServiceAreaOverrideStatus.PLANNED,
    )
}
