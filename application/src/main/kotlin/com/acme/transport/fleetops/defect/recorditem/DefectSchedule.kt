package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect schedule is in its life cycle. */
enum class DefectScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A defect schedule. Its code is unique within a tenant. */
data class DefectSchedule(
    var code: String,
    var title: String,
    var windowDays: Int,
    var label: String,
    var floorValue: Double,
    var carrier: String,
    var status: DefectScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectScheduleStatus): Boolean {
        when (status) {
            DefectScheduleStatus.PLANNED -> return next == DefectScheduleStatus.COMPLETED
            DefectScheduleStatus.SCHEDULED -> return next == DefectScheduleStatus.IN_PROGRESS
            DefectScheduleStatus.IN_PROGRESS -> return next == DefectScheduleStatus.COMPLETED
            DefectScheduleStatus.COMPLETED -> {
                when (next) {
                    DefectScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectScheduleStatus) {
        if (!canMoveTo(next)) {
            error("defect schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun effectiveTotal(): Double = round((windowDays.toDouble() + floorValue) * 100) / 100

    fun snapshot(): DefectSchedule {
        return DefectSchedule(
            code = code,
            title = title,
            windowDays = windowDays,
            label = label,
            floorValue = floorValue,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a defect schedule in status planned. */
fun newDefectSchedule(code: String, title: String): DefectSchedule {
    return DefectSchedule(
        code = code,
        title = title,
        windowDays = 0,
        label = "",
        floorValue = 0.0,
        carrier = "",
        status = DefectScheduleStatus.PLANNED,
    )
}
