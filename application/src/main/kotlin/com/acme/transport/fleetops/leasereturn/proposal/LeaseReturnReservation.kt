package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return reservation is in its life cycle. */
enum class LeaseReturnReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lease return reservation. Its code is unique within a tenant. */
data class LeaseReturnReservation(
    var code: String,
    var label: String,
    var depot: String,
    var loadKg: Double,
    var stopCount: Int,
    var vehicleId: String,
    var attempts: Int,
    var locale: String,
    var maximum: Double,
    var updatedAt: Int,
    var status: LeaseReturnReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnReservationStatus): Boolean {
        when (status) {
            LeaseReturnReservationStatus.OPEN -> {
                when (next) {
                    LeaseReturnReservationStatus.RESOLVED, LeaseReturnReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            LeaseReturnReservationStatus.IN_REVIEW -> {
                when (next) {
                    LeaseReturnReservationStatus.RESOLVED -> return true
                }
                return false
            }
            LeaseReturnReservationStatus.RESOLVED -> {
                when (next) {
                    LeaseReturnReservationStatus.REOPENED, LeaseReturnReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            LeaseReturnReservationStatus.REOPENED -> {
                when (next) {
                    LeaseReturnReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnReservationStatus) {
        if (!canMoveTo(next)) {
            error("lease return reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round(loadKg * updatedAt.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnReservation {
        return LeaseReturnReservation(
            code = code,
            label = label,
            depot = depot,
            loadKg = loadKg,
            stopCount = stopCount,
            vehicleId = vehicleId,
            attempts = attempts,
            locale = locale,
            maximum = maximum,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lease return reservation in status open. */
fun newLeaseReturnReservation(code: String, label: String): LeaseReturnReservation {
    return LeaseReturnReservation(
        code = code,
        label = label,
        depot = "",
        loadKg = 0.0,
        stopCount = 0,
        vehicleId = "",
        attempts = 0,
        locale = "",
        maximum = 0.0,
        updatedAt = 0,
        status = LeaseReturnReservationStatus.OPEN,
    )
}
