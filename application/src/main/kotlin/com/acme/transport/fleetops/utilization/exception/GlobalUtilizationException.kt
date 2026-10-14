package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global utilization exception records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a global utilization exception is in its life cycle. */
enum class GlobalUtilizationExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global utilization exception and its current status. */
data class GlobalUtilizationException(
    var code: String,
    var name: String,
    var discount: Double,
    var ownerId: String,
    var fuelLiters: Double,
    var channel: String,
    var driverId: String,
    var contactEmail: String,
    var depot: String,
    var status: GlobalUtilizationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalUtilizationExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalUtilizationExceptionStatus): Boolean {
        when (status) {
            GlobalUtilizationExceptionStatus.OPEN -> {
                when (next) {
                    GlobalUtilizationExceptionStatus.REOPENED, GlobalUtilizationExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalUtilizationExceptionStatus.IN_REVIEW -> return next == GlobalUtilizationExceptionStatus.RESOLVED || next == GlobalUtilizationExceptionStatus.REOPENED
            GlobalUtilizationExceptionStatus.RESOLVED -> return next == GlobalUtilizationExceptionStatus.REOPENED || next == GlobalUtilizationExceptionStatus.ARCHIVED
            GlobalUtilizationExceptionStatus.REOPENED -> {
                when (next) {
                    GlobalUtilizationExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalUtilizationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("global utilization exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(fuelLiters * discount * 100) / 100

    fun snapshot(): GlobalUtilizationException {
        return GlobalUtilizationException(
            code = code,
            name = name,
            discount = discount,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            channel = channel,
            driverId = driverId,
            contactEmail = contactEmail,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global utilization exception in status open. */
fun newGlobalUtilizationException(code: String, name: String): GlobalUtilizationException {
    return GlobalUtilizationException(
        code = code,
        name = name,
        discount = 0.0,
        ownerId = "",
        fuelLiters = 0.0,
        channel = "",
        driverId = "",
        contactEmail = "",
        depot = "",
        status = GlobalUtilizationExceptionStatus.OPEN,
    )
}
