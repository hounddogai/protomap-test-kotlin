package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection schedule is in its life cycle. */
enum class AdaptiveInspectionScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive inspection schedule as the inspection team keeps it. */
data class AdaptiveInspectionSchedule(
    var code: String,
    var title: String,
    var approved: Boolean,
    var description: String,
    var startsAt: Int,
    var phone: String,
    var vehicleId: String,
    var revision: Int,
    var ratio: Double,
    var status: AdaptiveInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionScheduleStatus): Boolean {
        when (status) {
            AdaptiveInspectionScheduleStatus.DRAFT -> return next == AdaptiveInspectionScheduleStatus.ACTIVE
            AdaptiveInspectionScheduleStatus.PENDING -> return next == AdaptiveInspectionScheduleStatus.ACTIVE
            AdaptiveInspectionScheduleStatus.ACTIVE -> {
                when (next) {
                    AdaptiveInspectionScheduleStatus.SUSPENDED, AdaptiveInspectionScheduleStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveInspectionScheduleStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveInspectionScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round((ratio + revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveInspectionSchedule {
        return AdaptiveInspectionSchedule(
            code = code,
            title = title,
            approved = approved,
            description = description,
            startsAt = startsAt,
            phone = phone,
            vehicleId = vehicleId,
            revision = revision,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive inspection schedule in status draft. */
fun newAdaptiveInspectionSchedule(code: String, title: String): AdaptiveInspectionSchedule {
    return AdaptiveInspectionSchedule(
        code = code,
        title = title,
        approved = false,
        description = "",
        startsAt = 0,
        phone = "",
        vehicleId = "",
        revision = 0,
        ratio = 0.0,
        status = AdaptiveInspectionScheduleStatus.DRAFT,
    )
}
