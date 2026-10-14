package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal locker adjustment is in its life cycle. */
enum class SeasonalLockerAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal locker adjustment. Its code is unique within a tenant. */
data class SeasonalLockerAdjustment(
    var code: String,
    var label: String,
    var graceDays: Int,
    var trailerId: String,
    var durationMinutes: Int,
    var odometer: Int,
    var windowDays: Int,
    var notes: String,
    var unit: String,
    var status: SeasonalLockerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLockerAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLockerAdjustmentStatus): Boolean {
        when (status) {
            SeasonalLockerAdjustmentStatus.PROPOSED -> {
                when (next) {
                    SeasonalLockerAdjustmentStatus.APPROVED -> return true
                }
                return false
            }
            SeasonalLockerAdjustmentStatus.APPROVED -> return next == SeasonalLockerAdjustmentStatus.WITHDRAWN
            SeasonalLockerAdjustmentStatus.REJECTED -> return next == SeasonalLockerAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalLockerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal locker adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round(windowDays.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): SeasonalLockerAdjustment {
        return SeasonalLockerAdjustment(
            code = code,
            label = label,
            graceDays = graceDays,
            trailerId = trailerId,
            durationMinutes = durationMinutes,
            odometer = odometer,
            windowDays = windowDays,
            notes = notes,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal locker adjustment in status proposed. */
fun newSeasonalLockerAdjustment(code: String, label: String): SeasonalLockerAdjustment {
    return SeasonalLockerAdjustment(
        code = code,
        label = label,
        graceDays = 0,
        trailerId = "",
        durationMinutes = 0,
        odometer = 0,
        windowDays = 0,
        notes = "",
        unit = "",
        status = SeasonalLockerAdjustmentStatus.PROPOSED,
    )
}
