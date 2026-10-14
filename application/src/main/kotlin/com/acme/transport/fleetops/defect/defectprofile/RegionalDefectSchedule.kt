package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.normalize

/** Where a regional defect schedule is in its life cycle. */
enum class RegionalDefectScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional defect schedule, with what the planners need to act on it. */
data class RegionalDefectSchedule(
    var code: String,
    var label: String,
    var contactEmail: String,
    var driverId: String,
    var durationMinutes: Int,
    var status: RegionalDefectScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectScheduleStatus): Boolean {
        when (status) {
            RegionalDefectScheduleStatus.REQUESTED -> {
                when (next) {
                    RegionalDefectScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalDefectScheduleStatus.GRANTED -> return next == RegionalDefectScheduleStatus.REVOKED
            RegionalDefectScheduleStatus.REVOKED -> {
                when (next) {
                    RegionalDefectScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDefectScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional defect schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): RegionalDefectSchedule {
        return RegionalDefectSchedule(
            code = code,
            label = label,
            contactEmail = contactEmail,
            driverId = driverId,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional defect schedule in status requested. */
fun newRegionalDefectSchedule(code: String, label: String): RegionalDefectSchedule {
    return RegionalDefectSchedule(
        code = code,
        label = label,
        contactEmail = "",
        driverId = "",
        durationMinutes = 0,
        status = RegionalDefectScheduleStatus.REQUESTED,
    )
}
