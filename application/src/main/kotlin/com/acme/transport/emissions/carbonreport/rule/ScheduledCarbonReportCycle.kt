package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled carbon report cycle is in its life cycle. */
enum class ScheduledCarbonReportCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled carbon report cycle and its current status. */
data class ScheduledCarbonReportCycle(
    var code: String,
    var title: String,
    var shiftCode: String,
    var weight: Double,
    var stopCount: Int,
    var capacity: Int,
    var sequence: Int,
    var windowDays: Int,
    var summary: String,
    var status: ScheduledCarbonReportCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCarbonReportCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCarbonReportCycleStatus): Boolean {
        when (status) {
            ScheduledCarbonReportCycleStatus.DRAFT -> {
                when (next) {
                    ScheduledCarbonReportCycleStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledCarbonReportCycleStatus.PENDING -> {
                when (next) {
                    ScheduledCarbonReportCycleStatus.ACTIVE, ScheduledCarbonReportCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledCarbonReportCycleStatus.ACTIVE -> {
                when (next) {
                    ScheduledCarbonReportCycleStatus.SUSPENDED, ScheduledCarbonReportCycleStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledCarbonReportCycleStatus.SUSPENDED -> return next == ScheduledCarbonReportCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledCarbonReportCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled carbon report cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(weight * windowDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledCarbonReportCycle {
        return ScheduledCarbonReportCycle(
            code = code,
            title = title,
            shiftCode = shiftCode,
            weight = weight,
            stopCount = stopCount,
            capacity = capacity,
            sequence = sequence,
            windowDays = windowDays,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled carbon report cycle in status draft. */
fun newScheduledCarbonReportCycle(code: String, title: String): ScheduledCarbonReportCycle {
    return ScheduledCarbonReportCycle(
        code = code,
        title = title,
        shiftCode = "",
        weight = 0.0,
        stopCount = 0,
        capacity = 0,
        sequence = 0,
        windowDays = 0,
        summary = "",
        status = ScheduledCarbonReportCycleStatus.DRAFT,
    )
}
