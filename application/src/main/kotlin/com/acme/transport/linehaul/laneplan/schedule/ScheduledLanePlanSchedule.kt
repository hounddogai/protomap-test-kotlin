package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled lane plan schedule records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a scheduled lane plan schedule is in its life cycle. */
enum class ScheduledLanePlanScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled lane plan schedule as the lane plan team keeps it. */
data class ScheduledLanePlanSchedule(
    var code: String,
    var label: String,
    var threshold: Double,
    var locale: String,
    var category: String,
    var segment: String,
    var margin: Double,
    var name: String,
    var routeCode: String,
    var status: ScheduledLanePlanScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanScheduleStatus): Boolean {
        when (status) {
            ScheduledLanePlanScheduleStatus.CREATED -> return next == ScheduledLanePlanScheduleStatus.PUBLISHED || next == ScheduledLanePlanScheduleStatus.RETIRED
            ScheduledLanePlanScheduleStatus.VALIDATED -> {
                when (next) {
                    ScheduledLanePlanScheduleStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledLanePlanScheduleStatus.PUBLISHED -> {
                when (next) {
                    ScheduledLanePlanScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((margin - threshold) * 100) / 100

    fun snapshot(): ScheduledLanePlanSchedule {
        return ScheduledLanePlanSchedule(
            code = code,
            label = label,
            threshold = threshold,
            locale = locale,
            category = category,
            segment = segment,
            margin = margin,
            name = name,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled lane plan schedule in status created. */
fun newScheduledLanePlanSchedule(code: String, label: String): ScheduledLanePlanSchedule {
    return ScheduledLanePlanSchedule(
        code = code,
        label = label,
        threshold = 0.0,
        locale = "",
        category = "",
        segment = "",
        margin = 0.0,
        name = "",
        routeCode = "",
        status = ScheduledLanePlanScheduleStatus.CREATED,
    )
}
