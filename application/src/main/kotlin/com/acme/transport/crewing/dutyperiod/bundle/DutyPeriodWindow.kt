package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period window is in its life cycle. */
enum class DutyPeriodWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One duty period window, with what the planners need to act on it. */
data class DutyPeriodWindow(
    var code: String,
    var label: String,
    var driverId: String,
    var windowDays: Int,
    var ownerId: String,
    var reviewer: String,
    var segment: String,
    var description: String,
    var margin: Double,
    var visible: Boolean,
    var status: DutyPeriodWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodWindowStatus): Boolean {
        when (status) {
            DutyPeriodWindowStatus.PROPOSED -> return next == DutyPeriodWindowStatus.REJECTED || next == DutyPeriodWindowStatus.WITHDRAWN
            DutyPeriodWindowStatus.APPROVED -> {
                when (next) {
                    DutyPeriodWindowStatus.REJECTED, DutyPeriodWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodWindowStatus.REJECTED -> {
                when (next) {
                    DutyPeriodWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodWindowStatus) {
        if (!canMoveTo(next)) {
            error("duty period window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round(margin * windowDays.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodWindow {
        return DutyPeriodWindow(
            code = code,
            label = label,
            driverId = driverId,
            windowDays = windowDays,
            ownerId = ownerId,
            reviewer = reviewer,
            segment = segment,
            description = description,
            margin = margin,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a duty period window in status proposed. */
fun newDutyPeriodWindow(code: String, label: String): DutyPeriodWindow {
    return DutyPeriodWindow(
        code = code,
        label = label,
        driverId = "",
        windowDays = 0,
        ownerId = "",
        reviewer = "",
        segment = "",
        description = "",
        margin = 0.0,
        visible = false,
        status = DutyPeriodWindowStatus.PROPOSED,
    )
}
