package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered capacity reservation records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a tiered capacity reservation is in its life cycle. */
enum class TieredCapacityReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered capacity reservation. Its code is unique within a tenant. */
data class TieredCapacityReservation(
    var code: String,
    var label: String,
    var country: String,
    var contactEmail: String,
    var discount: Double,
    var threshold: Double,
    var source: String,
    var status: TieredCapacityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCapacityReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCapacityReservationStatus): Boolean {
        when (status) {
            TieredCapacityReservationStatus.PROPOSED -> return next == TieredCapacityReservationStatus.REJECTED || next == TieredCapacityReservationStatus.WITHDRAWN
            TieredCapacityReservationStatus.APPROVED -> {
                when (next) {
                    TieredCapacityReservationStatus.REJECTED -> return true
                }
                return false
            }
            TieredCapacityReservationStatus.REJECTED -> return next == TieredCapacityReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredCapacityReservationStatus) {
        if (!canMoveTo(next)) {
            error("tiered capacity reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round(threshold * discount * 100) / 100

    fun snapshot(): TieredCapacityReservation {
        return TieredCapacityReservation(
            code = code,
            label = label,
            country = country,
            contactEmail = contactEmail,
            discount = discount,
            threshold = threshold,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered capacity reservation in status proposed. */
fun newTieredCapacityReservation(code: String, label: String): TieredCapacityReservation {
    return TieredCapacityReservation(
        code = code,
        label = label,
        country = "",
        contactEmail = "",
        discount = 0.0,
        threshold = 0.0,
        source = "",
        status = TieredCapacityReservationStatus.PROPOSED,
    )
}
