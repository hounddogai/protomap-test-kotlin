package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability allocation is in its life cycle. */
enum class AvailabilityAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A availability allocation. Its code is unique within a tenant. */
data class AvailabilityAllocation(
    var code: String,
    var title: String,
    var reviewer: String,
    var odometer: Int,
    var phone: String,
    var pallets: Int,
    var attempts: Int,
    var durationMinutes: Int,
    var attributes: MutableMap<String, String>,
    var status: AvailabilityAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityAllocationStatus): Boolean {
        when (status) {
            AvailabilityAllocationStatus.PLANNED -> return next == AvailabilityAllocationStatus.IN_PROGRESS
            AvailabilityAllocationStatus.SCHEDULED -> {
                when (next) {
                    AvailabilityAllocationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AvailabilityAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    AvailabilityAllocationStatus.COMPLETED, AvailabilityAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityAllocationStatus.COMPLETED -> return next == AvailabilityAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AvailabilityAllocationStatus) {
        if (!canMoveTo(next)) {
            error("availability allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"availability allocation"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((odometer.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityAllocation {
        return AvailabilityAllocation(
            code = code,
            title = title,
            reviewer = reviewer,
            odometer = odometer,
            phone = phone,
            pallets = pallets,
            attempts = attempts,
            durationMinutes = durationMinutes,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability allocation in status planned. */
fun newAvailabilityAllocation(code: String, title: String): AvailabilityAllocation {
    return AvailabilityAllocation(
        code = code,
        title = title,
        reviewer = "",
        odometer = 0,
        phone = "",
        pallets = 0,
        attempts = 0,
        durationMinutes = 0,
        attributes = mutableMapOf<String, String>(),
        status = AvailabilityAllocationStatus.PLANNED,
    )
}
