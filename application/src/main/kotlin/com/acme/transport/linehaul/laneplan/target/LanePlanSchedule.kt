package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan schedule is in its life cycle. */
enum class LanePlanScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lane plan schedule as the lane plan team keeps it. */
data class LanePlanSchedule(
    var code: String,
    var label: String,
    var channel: String,
    var revision: Int,
    var cost: Double,
    var vehicleId: String,
    var dock: String,
    var status: LanePlanScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanScheduleStatus): Boolean {
        when (status) {
            LanePlanScheduleStatus.REQUESTED -> {
                when (next) {
                    LanePlanScheduleStatus.GRANTED, LanePlanScheduleStatus.REVOKED -> return true
                }
                return false
            }
            LanePlanScheduleStatus.GRANTED -> {
                when (next) {
                    LanePlanScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            LanePlanScheduleStatus.REVOKED -> return next == LanePlanScheduleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LanePlanScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lane plan schedule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((revision.toDouble() + cost) * 100) / 100

    fun snapshot(): LanePlanSchedule {
        return LanePlanSchedule(
            code = code,
            label = label,
            channel = channel,
            revision = revision,
            cost = cost,
            vehicleId = vehicleId,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lane plan schedule in status requested. */
fun newLanePlanSchedule(code: String, label: String): LanePlanSchedule {
    return LanePlanSchedule(
        code = code,
        label = label,
        channel = "",
        revision = 0,
        cost = 0.0,
        vehicleId = "",
        dock = "",
        status = LanePlanScheduleStatus.REQUESTED,
    )
}
