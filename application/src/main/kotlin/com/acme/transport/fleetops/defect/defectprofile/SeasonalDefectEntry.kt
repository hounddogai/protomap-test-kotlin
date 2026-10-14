package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal defect entry is in its life cycle. */
enum class SeasonalDefectEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One seasonal defect entry, with what the planners need to act on it. */
data class SeasonalDefectEntry(
    var code: String,
    var title: String,
    var floorValue: Double,
    var odometer: Int,
    var routeCode: String,
    var status: SeasonalDefectEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDefectEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDefectEntryStatus): Boolean {
        when (status) {
            SeasonalDefectEntryStatus.REQUESTED -> return next == SeasonalDefectEntryStatus.EXPIRED
            SeasonalDefectEntryStatus.GRANTED -> {
                when (next) {
                    SeasonalDefectEntryStatus.REVOKED, SeasonalDefectEntryStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalDefectEntryStatus.REVOKED -> return next == SeasonalDefectEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalDefectEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal defect entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal defect entry"} $code"

    fun adjustedValue(): Double = round(floorValue * odometer.toDouble() * 100) / 100

    fun snapshot(): SeasonalDefectEntry {
        return SeasonalDefectEntry(
            code = code,
            title = title,
            floorValue = floorValue,
            odometer = odometer,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal defect entry in status requested. */
fun newSeasonalDefectEntry(code: String, title: String): SeasonalDefectEntry {
    return SeasonalDefectEntry(
        code = code,
        title = title,
        floorValue = 0.0,
        odometer = 0,
        routeCode = "",
        status = SeasonalDefectEntryStatus.REQUESTED,
    )
}
