package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability forecast records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability forecast is in its life cycle. */
enum class AvailabilityForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a availability forecast and its current status. */
data class AvailabilityForecast(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var weight: Double,
    var notes: String,
    var locale: String,
    var odometer: Int,
    var checksum: String,
    var channel: String,
    var sequence: Int,
    var status: AvailabilityForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityForecastStatus): Boolean {
        when (status) {
            AvailabilityForecastStatus.NEW -> {
                when (next) {
                    AvailabilityForecastStatus.QUEUED -> return true
                }
                return false
            }
            AvailabilityForecastStatus.QUEUED -> {
                when (next) {
                    AvailabilityForecastStatus.RUNNING, AvailabilityForecastStatus.DONE -> return true
                }
                return false
            }
            AvailabilityForecastStatus.RUNNING -> return next == AvailabilityForecastStatus.DONE || next == AvailabilityForecastStatus.FAILED
            AvailabilityForecastStatus.DONE -> {
                when (next) {
                    AvailabilityForecastStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityForecastStatus) {
        if (!canMoveTo(next)) {
            error("availability forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((fuelLiters - odometer.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityForecast {
        return AvailabilityForecast(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            weight = weight,
            notes = notes,
            locale = locale,
            odometer = odometer,
            checksum = checksum,
            channel = channel,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a availability forecast in status new. */
fun newAvailabilityForecast(code: String, label: String): AvailabilityForecast {
    return AvailabilityForecast(
        code = code,
        label = label,
        fuelLiters = 0.0,
        weight = 0.0,
        notes = "",
        locale = "",
        odometer = 0,
        checksum = "",
        channel = "",
        sequence = 0,
        status = AvailabilityForecastStatus.NEW,
    )
}
