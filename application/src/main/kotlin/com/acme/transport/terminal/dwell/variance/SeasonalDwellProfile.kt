package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dwell profile is in its life cycle. */
enum class SeasonalDwellProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal dwell profile as the dwell team keeps it. */
data class SeasonalDwellProfile(
    var code: String,
    var label: String,
    var driverId: String,
    var segment: String,
    var cost: Double,
    var source: String,
    var vehicleId: String,
    var unit: String,
    var score: Double,
    var batchSize: Int,
    var status: SeasonalDwellProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDwellProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDwellProfileStatus): Boolean {
        when (status) {
            SeasonalDwellProfileStatus.PLANNED -> return next == SeasonalDwellProfileStatus.SCHEDULED || next == SeasonalDwellProfileStatus.CANCELLED
            SeasonalDwellProfileStatus.SCHEDULED -> {
                when (next) {
                    SeasonalDwellProfileStatus.IN_PROGRESS, SeasonalDwellProfileStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalDwellProfileStatus.IN_PROGRESS -> return next == SeasonalDwellProfileStatus.COMPLETED
            SeasonalDwellProfileStatus.COMPLETED -> {
                when (next) {
                    SeasonalDwellProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDwellProfileStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dwell profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal dwell profile"} $code"

    fun weightedTotal(): Double = round(cost * batchSize.toDouble() * 100) / 100

    fun snapshot(): SeasonalDwellProfile {
        return SeasonalDwellProfile(
            code = code,
            label = label,
            driverId = driverId,
            segment = segment,
            cost = cost,
            source = source,
            vehicleId = vehicleId,
            unit = unit,
            score = score,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal dwell profile in status planned. */
fun newSeasonalDwellProfile(code: String, label: String): SeasonalDwellProfile {
    return SeasonalDwellProfile(
        code = code,
        label = label,
        driverId = "",
        segment = "",
        cost = 0.0,
        source = "",
        vehicleId = "",
        unit = "",
        score = 0.0,
        batchSize = 0,
        status = SeasonalDwellProfileStatus.PLANNED,
    )
}
