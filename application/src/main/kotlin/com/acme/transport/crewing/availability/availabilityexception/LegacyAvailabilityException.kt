package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy availability exception records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a legacy availability exception is in its life cycle. */
enum class LegacyAvailabilityExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy availability exception as the availability team keeps it. */
data class LegacyAvailabilityException(
    var code: String,
    var name: String,
    var revision: Int,
    var vehicleId: String,
    var margin: Double,
    var amount: Double,
    var expiresAt: Int,
    var status: LegacyAvailabilityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAvailabilityExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAvailabilityExceptionStatus): Boolean {
        when (status) {
            LegacyAvailabilityExceptionStatus.REQUESTED -> return next == LegacyAvailabilityExceptionStatus.GRANTED
            LegacyAvailabilityExceptionStatus.GRANTED -> {
                when (next) {
                    LegacyAvailabilityExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyAvailabilityExceptionStatus.REVOKED -> return next == LegacyAvailabilityExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyAvailabilityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("legacy availability exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"legacy availability exception"} $code"

    fun weightedValue(): Double = round((amount - revision.toDouble()) * 100) / 100

    fun snapshot(): LegacyAvailabilityException {
        return LegacyAvailabilityException(
            code = code,
            name = name,
            revision = revision,
            vehicleId = vehicleId,
            margin = margin,
            amount = amount,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy availability exception in status requested. */
fun newLegacyAvailabilityException(code: String, name: String): LegacyAvailabilityException {
    return LegacyAvailabilityException(
        code = code,
        name = name,
        revision = 0,
        vehicleId = "",
        margin = 0.0,
        amount = 0.0,
        expiresAt = 0,
        status = LegacyAvailabilityExceptionStatus.REQUESTED,
    )
}
