package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity reservation is in its life cycle. */
enum class CapacityReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capacity reservation as the capacity team keeps it. */
data class CapacityReservation(
    var code: String,
    var name: String,
    var driverId: String,
    var endsAt: Int,
    var etaMinutes: Int,
    var amount: Double,
    var graceDays: Int,
    var approved: Boolean,
    var capacity: Int,
    var carrier: String,
    var status: CapacityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityReservationStatus): Boolean {
        when (status) {
            CapacityReservationStatus.CREATED -> {
                when (next) {
                    CapacityReservationStatus.VALIDATED -> return true
                }
                return false
            }
            CapacityReservationStatus.VALIDATED -> {
                when (next) {
                    CapacityReservationStatus.RETIRED -> return true
                }
                return false
            }
            CapacityReservationStatus.PUBLISHED -> {
                when (next) {
                    CapacityReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityReservationStatus) {
        if (!canMoveTo(next)) {
            error("capacity reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round(capacity.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): CapacityReservation {
        return CapacityReservation(
            code = code,
            name = name,
            driverId = driverId,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            amount = amount,
            graceDays = graceDays,
            approved = approved,
            capacity = capacity,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capacity reservation in status created. */
fun newCapacityReservation(code: String, name: String): CapacityReservation {
    return CapacityReservation(
        code = code,
        name = name,
        driverId = "",
        endsAt = 0,
        etaMinutes = 0,
        amount = 0.0,
        graceDays = 0,
        approved = false,
        capacity = 0,
        carrier = "",
        status = CapacityReservationStatus.CREATED,
    )
}
