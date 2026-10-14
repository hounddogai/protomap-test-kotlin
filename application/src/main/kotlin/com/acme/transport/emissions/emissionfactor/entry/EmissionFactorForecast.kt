package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor forecast is in its life cycle. */
enum class EmissionFactorForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One emission factor forecast, with what the planners need to act on it. */
data class EmissionFactorForecast(
    var code: String,
    var label: String,
    var checksum: String,
    var odometer: Int,
    var dock: String,
    var distanceKm: Double,
    var contactEmail: String,
    var attempts: Int,
    var phone: String,
    var status: EmissionFactorForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorForecastStatus): Boolean {
        when (status) {
            EmissionFactorForecastStatus.IDLE -> {
                when (next) {
                    EmissionFactorForecastStatus.WARMING, EmissionFactorForecastStatus.STOPPED -> return true
                }
                return false
            }
            EmissionFactorForecastStatus.WARMING -> {
                when (next) {
                    EmissionFactorForecastStatus.DRAINING, EmissionFactorForecastStatus.STOPPED -> return true
                }
                return false
            }
            EmissionFactorForecastStatus.READY -> return next == EmissionFactorForecastStatus.DRAINING || next == EmissionFactorForecastStatus.STOPPED
            EmissionFactorForecastStatus.DRAINING -> {
                when (next) {
                    EmissionFactorForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorForecastStatus) {
        if (!canMoveTo(next)) {
            error("emission factor forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round(odometer.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorForecast {
        return EmissionFactorForecast(
            code = code,
            label = label,
            checksum = checksum,
            odometer = odometer,
            dock = dock,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            attempts = attempts,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a emission factor forecast in status idle. */
fun newEmissionFactorForecast(code: String, label: String): EmissionFactorForecast {
    return EmissionFactorForecast(
        code = code,
        label = label,
        checksum = "",
        odometer = 0,
        dock = "",
        distanceKm = 0.0,
        contactEmail = "",
        attempts = 0,
        phone = "",
        status = EmissionFactorForecastStatus.IDLE,
    )
}
