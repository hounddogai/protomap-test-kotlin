package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual near miss forecast is in its life cycle. */
enum class ManualNearMissForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One manual near miss forecast, with what the planners need to act on it. */
data class ManualNearMissForecast(
    var code: String,
    var label: String,
    var dock: String,
    var batchSize: Int,
    var quantity: Int,
    var minimum: Double,
    var stopCount: Int,
    var status: ManualNearMissForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualNearMissForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualNearMissForecastStatus): Boolean {
        when (status) {
            ManualNearMissForecastStatus.REQUESTED -> return next == ManualNearMissForecastStatus.REVOKED
            ManualNearMissForecastStatus.GRANTED -> {
                when (next) {
                    ManualNearMissForecastStatus.REVOKED, ManualNearMissForecastStatus.EXPIRED -> return true
                }
                return false
            }
            ManualNearMissForecastStatus.REVOKED -> return next == ManualNearMissForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualNearMissForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual near miss forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual near miss forecast"} $code"

    fun projectedVolume(): Double = round(stopCount.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): ManualNearMissForecast {
        return ManualNearMissForecast(
            code = code,
            label = label,
            dock = dock,
            batchSize = batchSize,
            quantity = quantity,
            minimum = minimum,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual near miss forecast in status requested. */
fun newManualNearMissForecast(code: String, label: String): ManualNearMissForecast {
    return ManualNearMissForecast(
        code = code,
        label = label,
        dock = "",
        batchSize = 0,
        quantity = 0,
        minimum = 0.0,
        stopCount = 0,
        status = ManualNearMissForecastStatus.REQUESTED,
    )
}
