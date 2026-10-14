package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered scorecard schedule is in its life cycle. */
enum class TieredScorecardScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered scorecard schedule and its current status. */
data class TieredScorecardSchedule(
    var code: String,
    var title: String,
    var vehicleId: String,
    var description: String,
    var pinned: Boolean,
    var floorValue: Double,
    var checksum: String,
    var archived: Boolean,
    var carrier: String,
    var durationMinutes: Int,
    var status: TieredScorecardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredScorecardScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredScorecardScheduleStatus): Boolean {
        when (status) {
            TieredScorecardScheduleStatus.CREATED -> return next == TieredScorecardScheduleStatus.VALIDATED || next == TieredScorecardScheduleStatus.PUBLISHED
            TieredScorecardScheduleStatus.VALIDATED -> {
                when (next) {
                    TieredScorecardScheduleStatus.RETIRED -> return true
                }
                return false
            }
            TieredScorecardScheduleStatus.PUBLISHED -> return next == TieredScorecardScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredScorecardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tiered scorecard schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered scorecard schedule"} $code"

    fun effectiveExposure(): Double = round(durationMinutes.toDouble() * floorValue * 100) / 100

    fun snapshot(): TieredScorecardSchedule {
        return TieredScorecardSchedule(
            code = code,
            title = title,
            vehicleId = vehicleId,
            description = description,
            pinned = pinned,
            floorValue = floorValue,
            checksum = checksum,
            archived = archived,
            carrier = carrier,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered scorecard schedule in status created. */
fun newTieredScorecardSchedule(code: String, title: String): TieredScorecardSchedule {
    return TieredScorecardSchedule(
        code = code,
        title = title,
        vehicleId = "",
        description = "",
        pinned = false,
        floorValue = 0.0,
        checksum = "",
        archived = false,
        carrier = "",
        durationMinutes = 0,
        status = TieredScorecardScheduleStatus.CREATED,
    )
}
