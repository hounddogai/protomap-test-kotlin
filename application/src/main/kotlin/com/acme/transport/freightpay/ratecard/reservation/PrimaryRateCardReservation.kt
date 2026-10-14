package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary rate card reservation records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a primary rate card reservation is in its life cycle. */
enum class PrimaryRateCardReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary rate card reservation as the rate card team keeps it. */
data class PrimaryRateCardReservation(
    var code: String,
    var label: String,
    var locale: String,
    var unit: String,
    var approved: Boolean,
    var priority: Int,
    var version: Int,
    var region: String,
    var vehicleId: String,
    var currency: String,
    var cost: Double,
    var status: PrimaryRateCardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRateCardReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRateCardReservationStatus): Boolean {
        when (status) {
            PrimaryRateCardReservationStatus.OPEN -> return next == PrimaryRateCardReservationStatus.RESOLVED
            PrimaryRateCardReservationStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryRateCardReservationStatus.RESOLVED -> return true
                }
                return false
            }
            PrimaryRateCardReservationStatus.RESOLVED -> return next == PrimaryRateCardReservationStatus.ARCHIVED
            PrimaryRateCardReservationStatus.REOPENED -> return next == PrimaryRateCardReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryRateCardReservationStatus) {
        if (!canMoveTo(next)) {
            error("primary rate card reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((cost - version.toDouble()) * 100) / 100

    fun snapshot(): PrimaryRateCardReservation {
        return PrimaryRateCardReservation(
            code = code,
            label = label,
            locale = locale,
            unit = unit,
            approved = approved,
            priority = priority,
            version = version,
            region = region,
            vehicleId = vehicleId,
            currency = currency,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary rate card reservation in status open. */
fun newPrimaryRateCardReservation(code: String, label: String): PrimaryRateCardReservation {
    return PrimaryRateCardReservation(
        code = code,
        label = label,
        locale = "",
        unit = "",
        approved = false,
        priority = 0,
        version = 0,
        region = "",
        vehicleId = "",
        currency = "",
        cost = 0.0,
        status = PrimaryRateCardReservationStatus.OPEN,
    )
}
