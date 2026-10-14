package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling drop schedule is in its life cycle. */
enum class RollingDropScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling drop schedule as the drop team keeps it. */
data class RollingDropSchedule(
    var code: String,
    var label: String,
    var shiftCode: String,
    var weight: Double,
    var budget: Double,
    var capacity: Int,
    var region: String,
    var status: RollingDropScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDropScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDropScheduleStatus): Boolean {
        when (status) {
            RollingDropScheduleStatus.REQUESTED -> {
                when (next) {
                    RollingDropScheduleStatus.GRANTED, RollingDropScheduleStatus.REVOKED -> return true
                }
                return false
            }
            RollingDropScheduleStatus.GRANTED -> {
                when (next) {
                    RollingDropScheduleStatus.REVOKED -> return true
                }
                return false
            }
            RollingDropScheduleStatus.REVOKED -> {
                when (next) {
                    RollingDropScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDropScheduleStatus) {
        if (!canMoveTo(next)) {
            error("rolling drop schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((budget + weight) * 100) / 100

    fun snapshot(): RollingDropSchedule {
        return RollingDropSchedule(
            code = code,
            label = label,
            shiftCode = shiftCode,
            weight = weight,
            budget = budget,
            capacity = capacity,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling drop schedule in status requested. */
fun newRollingDropSchedule(code: String, label: String): RollingDropSchedule {
    return RollingDropSchedule(
        code = code,
        label = label,
        shiftCode = "",
        weight = 0.0,
        budget = 0.0,
        capacity = 0,
        region = "",
        status = RollingDropScheduleStatus.REQUESTED,
    )
}
