package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending qualification forecast is in its life cycle. */
enum class PendingQualificationForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending qualification forecast as the qualification team keeps it. */
data class PendingQualificationForecast(
    var code: String,
    var title: String,
    var floorValue: Double,
    var score: Double,
    var vehicleId: String,
    var status: PendingQualificationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationForecastStatus): Boolean {
        when (status) {
            PendingQualificationForecastStatus.IDLE -> {
                when (next) {
                    PendingQualificationForecastStatus.READY, PendingQualificationForecastStatus.DRAINING -> return true
                }
                return false
            }
            PendingQualificationForecastStatus.WARMING -> {
                when (next) {
                    PendingQualificationForecastStatus.READY -> return true
                }
                return false
            }
            PendingQualificationForecastStatus.READY -> return next == PendingQualificationForecastStatus.STOPPED
            PendingQualificationForecastStatus.DRAINING -> {
                when (next) {
                    PendingQualificationForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingQualificationForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending qualification forecast"} $code"

    fun projectedVolume(): Double = round((score - floorValue) * 100) / 100

    fun snapshot(): PendingQualificationForecast {
        return PendingQualificationForecast(
            code = code,
            title = title,
            floorValue = floorValue,
            score = score,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending qualification forecast in status idle. */
fun newPendingQualificationForecast(code: String, title: String): PendingQualificationForecast {
    return PendingQualificationForecast(
        code = code,
        title = title,
        floorValue = 0.0,
        score = 0.0,
        vehicleId = "",
        status = PendingQualificationForecastStatus.IDLE,
    )
}
