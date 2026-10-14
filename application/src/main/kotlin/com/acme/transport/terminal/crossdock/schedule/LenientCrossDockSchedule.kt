package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient cross dock schedule records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a lenient cross dock schedule is in its life cycle. */
enum class LenientCrossDockScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient cross dock schedule as the cross dock team keeps it. */
data class LenientCrossDockSchedule(
    var code: String,
    var name: String,
    var pallets: Int,
    var startsAt: Int,
    var unit: String,
    var capacity: Int,
    var phone: String,
    var distanceKm: Double,
    var discount: Double,
    var vehicleId: String,
    var expiresAt: Int,
    var status: LenientCrossDockScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCrossDockScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCrossDockScheduleStatus): Boolean {
        when (status) {
            LenientCrossDockScheduleStatus.PROPOSED -> return next == LenientCrossDockScheduleStatus.REJECTED
            LenientCrossDockScheduleStatus.APPROVED -> return next == LenientCrossDockScheduleStatus.REJECTED
            LenientCrossDockScheduleStatus.REJECTED -> return next == LenientCrossDockScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientCrossDockScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lenient cross dock schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round(capacity.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): LenientCrossDockSchedule {
        return LenientCrossDockSchedule(
            code = code,
            name = name,
            pallets = pallets,
            startsAt = startsAt,
            unit = unit,
            capacity = capacity,
            phone = phone,
            distanceKm = distanceKm,
            discount = discount,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient cross dock schedule in status proposed. */
fun newLenientCrossDockSchedule(code: String, name: String): LenientCrossDockSchedule {
    return LenientCrossDockSchedule(
        code = code,
        name = name,
        pallets = 0,
        startsAt = 0,
        unit = "",
        capacity = 0,
        phone = "",
        distanceKm = 0.0,
        discount = 0.0,
        vehicleId = "",
        expiresAt = 0,
        status = LenientCrossDockScheduleStatus.PROPOSED,
    )
}
