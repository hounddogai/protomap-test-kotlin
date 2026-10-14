package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report reservation records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report reservation is in its life cycle. */
enum class CarbonReportReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a carbon report reservation and its current status. */
data class CarbonReportReservation(
    var code: String,
    var name: String,
    var retries: Int,
    var channel: String,
    var driverId: String,
    var notes: String,
    var margin: Double,
    var status: CarbonReportReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportReservationStatus): Boolean {
        when (status) {
            CarbonReportReservationStatus.PLANNED -> {
                when (next) {
                    CarbonReportReservationStatus.COMPLETED -> return true
                }
                return false
            }
            CarbonReportReservationStatus.SCHEDULED -> return next == CarbonReportReservationStatus.IN_PROGRESS || next == CarbonReportReservationStatus.COMPLETED
            CarbonReportReservationStatus.IN_PROGRESS -> {
                when (next) {
                    CarbonReportReservationStatus.COMPLETED -> return true
                }
                return false
            }
            CarbonReportReservationStatus.COMPLETED -> {
                when (next) {
                    CarbonReportReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportReservationStatus) {
        if (!canMoveTo(next)) {
            error("carbon report reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round((retries.toDouble() - margin) * 100) / 100

    fun snapshot(): CarbonReportReservation {
        return CarbonReportReservation(
            code = code,
            name = name,
            retries = retries,
            channel = channel,
            driverId = driverId,
            notes = notes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a carbon report reservation in status planned. */
fun newCarbonReportReservation(code: String, name: String): CarbonReportReservation {
    return CarbonReportReservation(
        code = code,
        name = name,
        retries = 0,
        channel = "",
        driverId = "",
        notes = "",
        margin = 0.0,
        status = CarbonReportReservationStatus.PLANNED,
    )
}
