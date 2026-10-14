package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell forecast is in its life cycle. */
enum class DwellForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dwell forecast and its current status. */
data class DwellForecast(
    var code: String,
    var title: String,
    var notes: String,
    var country: String,
    var retries: Int,
    var label: String,
    var revision: Int,
    var ownerId: String,
    var status: DwellForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellForecastStatus): Boolean {
        when (status) {
            DwellForecastStatus.REQUESTED -> return next == DwellForecastStatus.GRANTED || next == DwellForecastStatus.REVOKED
            DwellForecastStatus.GRANTED -> {
                when (next) {
                    DwellForecastStatus.REVOKED, DwellForecastStatus.EXPIRED -> return true
                }
                return false
            }
            DwellForecastStatus.REVOKED -> return next == DwellForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DwellForecastStatus) {
        if (!canMoveTo(next)) {
            error("dwell forecast $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"dwell forecast"} $code"

    fun projectedExposure(): Double = round(retries.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): DwellForecast {
        return DwellForecast(
            code = code,
            title = title,
            notes = notes,
            country = country,
            retries = retries,
            label = label,
            revision = revision,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dwell forecast in status requested. */
fun newDwellForecast(code: String, title: String): DwellForecast {
    return DwellForecast(
        code = code,
        title = title,
        notes = "",
        country = "",
        retries = 0,
        label = "",
        revision = 0,
        ownerId = "",
        status = DwellForecastStatus.REQUESTED,
    )
}
