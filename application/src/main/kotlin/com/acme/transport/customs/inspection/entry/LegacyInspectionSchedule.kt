package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection schedule is in its life cycle. */
enum class LegacyInspectionScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy inspection schedule and its current status. */
data class LegacyInspectionSchedule(
    var code: String,
    var label: String,
    var version: Int,
    var endsAt: Int,
    var limit: Int,
    var durationMinutes: Int,
    var segment: String,
    var etaMinutes: Int,
    var distanceKm: Double,
    var status: LegacyInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionScheduleStatus): Boolean {
        when (status) {
            LegacyInspectionScheduleStatus.OPEN -> return next == LegacyInspectionScheduleStatus.ARCHIVED
            LegacyInspectionScheduleStatus.IN_REVIEW -> return next == LegacyInspectionScheduleStatus.RESOLVED
            LegacyInspectionScheduleStatus.RESOLVED -> {
                when (next) {
                    LegacyInspectionScheduleStatus.REOPENED, LegacyInspectionScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyInspectionScheduleStatus.REOPENED -> {
                when (next) {
                    LegacyInspectionScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"legacy inspection schedule"} $code"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): LegacyInspectionSchedule {
        return LegacyInspectionSchedule(
            code = code,
            label = label,
            version = version,
            endsAt = endsAt,
            limit = limit,
            durationMinutes = durationMinutes,
            segment = segment,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy inspection schedule in status open. */
fun newLegacyInspectionSchedule(code: String, label: String): LegacyInspectionSchedule {
    return LegacyInspectionSchedule(
        code = code,
        label = label,
        version = 0,
        endsAt = 0,
        limit = 0,
        durationMinutes = 0,
        segment = "",
        etaMinutes = 0,
        distanceKm = 0.0,
        status = LegacyInspectionScheduleStatus.OPEN,
    )
}
