package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cutoff schedule is in its life cycle. */
enum class CappedCutoffScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped cutoff schedule and its current status. */
data class CappedCutoffSchedule(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var active: Boolean,
    var shiftCode: String,
    var minimum: Double,
    var trailerId: String,
    var carrier: String,
    var vehicleId: String,
    var status: CappedCutoffScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCutoffScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCutoffScheduleStatus): Boolean {
        when (status) {
            CappedCutoffScheduleStatus.PLANNED -> {
                when (next) {
                    CappedCutoffScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            CappedCutoffScheduleStatus.SCHEDULED -> return next == CappedCutoffScheduleStatus.IN_PROGRESS
            CappedCutoffScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    CappedCutoffScheduleStatus.COMPLETED, CappedCutoffScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            CappedCutoffScheduleStatus.COMPLETED -> {
                when (next) {
                    CappedCutoffScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCutoffScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capped cutoff schedule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round((minimum + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CappedCutoffSchedule {
        return CappedCutoffSchedule(
            code = code,
            name = name,
            updatedAt = updatedAt,
            active = active,
            shiftCode = shiftCode,
            minimum = minimum,
            trailerId = trailerId,
            carrier = carrier,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped cutoff schedule in status planned. */
fun newCappedCutoffSchedule(code: String, name: String): CappedCutoffSchedule {
    return CappedCutoffSchedule(
        code = code,
        name = name,
        updatedAt = 0,
        active = false,
        shiftCode = "",
        minimum = 0.0,
        trailerId = "",
        carrier = "",
        vehicleId = "",
        status = CappedCutoffScheduleStatus.PLANNED,
    )
}
