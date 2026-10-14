package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending scorecard forecast is in its life cycle. */
enum class PendingScorecardForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending scorecard forecast, with what the planners need to act on it. */
data class PendingScorecardForecast(
    var code: String,
    var name: String,
    var windowDays: Int,
    var contactEmail: String,
    var shiftCode: String,
    var expiresAt: Int,
    var status: PendingScorecardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingScorecardForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingScorecardForecastStatus): Boolean {
        when (status) {
            PendingScorecardForecastStatus.REQUESTED -> {
                when (next) {
                    PendingScorecardForecastStatus.GRANTED -> return true
                }
                return false
            }
            PendingScorecardForecastStatus.GRANTED -> {
                when (next) {
                    PendingScorecardForecastStatus.EXPIRED -> return true
                }
                return false
            }
            PendingScorecardForecastStatus.REVOKED -> {
                when (next) {
                    PendingScorecardForecastStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingScorecardForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending scorecard forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round((windowDays.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PendingScorecardForecast {
        return PendingScorecardForecast(
            code = code,
            name = name,
            windowDays = windowDays,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending scorecard forecast in status requested. */
fun newPendingScorecardForecast(code: String, name: String): PendingScorecardForecast {
    return PendingScorecardForecast(
        code = code,
        name = name,
        windowDays = 0,
        contactEmail = "",
        shiftCode = "",
        expiresAt = 0,
        status = PendingScorecardForecastStatus.REQUESTED,
    )
}
