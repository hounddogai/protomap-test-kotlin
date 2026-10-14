package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection schedule is in its life cycle. */
enum class PendingInspectionScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending inspection schedule. Its code is unique within a tenant. */
data class PendingInspectionSchedule(
    var code: String,
    var title: String,
    var loadKg: Double,
    var score: Double,
    var depot: String,
    var rate: Double,
    var odometer: Int,
    var floorValue: Double,
    var status: PendingInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionScheduleStatus): Boolean {
        when (status) {
            PendingInspectionScheduleStatus.CREATED -> return next == PendingInspectionScheduleStatus.RETIRED
            PendingInspectionScheduleStatus.VALIDATED -> return next == PendingInspectionScheduleStatus.PUBLISHED
            PendingInspectionScheduleStatus.PUBLISHED -> {
                when (next) {
                    PendingInspectionScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(odometer.toDouble() * loadKg * 100) / 100

    fun snapshot(): PendingInspectionSchedule {
        return PendingInspectionSchedule(
            code = code,
            title = title,
            loadKg = loadKg,
            score = score,
            depot = depot,
            rate = rate,
            odometer = odometer,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending inspection schedule in status created. */
fun newPendingInspectionSchedule(code: String, title: String): PendingInspectionSchedule {
    return PendingInspectionSchedule(
        code = code,
        title = title,
        loadKg = 0.0,
        score = 0.0,
        depot = "",
        rate = 0.0,
        odometer = 0,
        floorValue = 0.0,
        status = PendingInspectionScheduleStatus.CREATED,
    )
}
