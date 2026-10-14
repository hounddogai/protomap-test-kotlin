package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual violation forecast is in its life cycle. */
enum class ManualViolationForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual violation forecast. Its code is unique within a tenant. */
data class ManualViolationForecast(
    var code: String,
    var label: String,
    var name: String,
    var etaMinutes: Int,
    var revision: Int,
    var ceiling: Double,
    var status: ManualViolationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualViolationForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualViolationForecastStatus): Boolean {
        when (status) {
            ManualViolationForecastStatus.IDLE -> return next == ManualViolationForecastStatus.READY
            ManualViolationForecastStatus.WARMING -> return next == ManualViolationForecastStatus.STOPPED
            ManualViolationForecastStatus.READY -> return next == ManualViolationForecastStatus.DRAINING
            ManualViolationForecastStatus.DRAINING -> return next == ManualViolationForecastStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualViolationForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual violation forecast $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round((ceiling + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ManualViolationForecast {
        return ManualViolationForecast(
            code = code,
            label = label,
            name = name,
            etaMinutes = etaMinutes,
            revision = revision,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual violation forecast in status idle. */
fun newManualViolationForecast(code: String, label: String): ManualViolationForecast {
    return ManualViolationForecast(
        code = code,
        label = label,
        name = "",
        etaMinutes = 0,
        revision = 0,
        ceiling = 0.0,
        status = ManualViolationForecastStatus.IDLE,
    )
}
