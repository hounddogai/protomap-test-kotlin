package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive clearance reservation records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a adaptive clearance reservation is in its life cycle. */
enum class AdaptiveClearanceReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive clearance reservation. Its code is unique within a tenant. */
data class AdaptiveClearanceReservation(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var dock: String,
    var endsAt: Int,
    var budget: Double,
    var pallets: Int,
    var amount: Double,
    var name: String,
    var ceiling: Double,
    var status: AdaptiveClearanceReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveClearanceReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveClearanceReservationStatus): Boolean {
        when (status) {
            AdaptiveClearanceReservationStatus.OPEN -> {
                when (next) {
                    AdaptiveClearanceReservationStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveClearanceReservationStatus.IN_REVIEW -> return next == AdaptiveClearanceReservationStatus.RESOLVED || next == AdaptiveClearanceReservationStatus.REOPENED
            AdaptiveClearanceReservationStatus.RESOLVED -> return next == AdaptiveClearanceReservationStatus.REOPENED
            AdaptiveClearanceReservationStatus.REOPENED -> return next == AdaptiveClearanceReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveClearanceReservationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive clearance reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((pallets.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveClearanceReservation {
        return AdaptiveClearanceReservation(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            dock = dock,
            endsAt = endsAt,
            budget = budget,
            pallets = pallets,
            amount = amount,
            name = name,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive clearance reservation in status open. */
fun newAdaptiveClearanceReservation(code: String, label: String): AdaptiveClearanceReservation {
    return AdaptiveClearanceReservation(
        code = code,
        label = label,
        fuelLiters = 0.0,
        dock = "",
        endsAt = 0,
        budget = 0.0,
        pallets = 0,
        amount = 0.0,
        name = "",
        ceiling = 0.0,
        status = AdaptiveClearanceReservationStatus.OPEN,
    )
}
