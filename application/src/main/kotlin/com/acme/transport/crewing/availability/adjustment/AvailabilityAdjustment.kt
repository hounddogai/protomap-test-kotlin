package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability adjustment records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability adjustment is in its life cycle. */
enum class AvailabilityAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A availability adjustment. Its code is unique within a tenant. */
data class AvailabilityAdjustment(
    var code: String,
    var title: String,
    var phone: String,
    var vehicleId: String,
    var startsAt: Int,
    var endsAt: Int,
    var minimum: Double,
    var label: String,
    var status: AvailabilityAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityAdjustmentStatus): Boolean {
        when (status) {
            AvailabilityAdjustmentStatus.REQUESTED -> return next == AvailabilityAdjustmentStatus.REVOKED
            AvailabilityAdjustmentStatus.GRANTED -> return next == AvailabilityAdjustmentStatus.REVOKED || next == AvailabilityAdjustmentStatus.EXPIRED
            AvailabilityAdjustmentStatus.REVOKED -> {
                when (next) {
                    AvailabilityAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("availability adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"availability adjustment"} $code"

    fun projectedValue(): Double = round((endsAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityAdjustment {
        return AvailabilityAdjustment(
            code = code,
            title = title,
            phone = phone,
            vehicleId = vehicleId,
            startsAt = startsAt,
            endsAt = endsAt,
            minimum = minimum,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability adjustment in status requested. */
fun newAvailabilityAdjustment(code: String, title: String): AvailabilityAdjustment {
    return AvailabilityAdjustment(
        code = code,
        title = title,
        phone = "",
        vehicleId = "",
        startsAt = 0,
        endsAt = 0,
        minimum = 0.0,
        label = "",
        status = AvailabilityAdjustmentStatus.REQUESTED,
    )
}
