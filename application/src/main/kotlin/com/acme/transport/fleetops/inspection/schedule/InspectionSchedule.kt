package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection schedule records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection schedule is in its life cycle. */
enum class InspectionScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One inspection schedule, with what the planners need to act on it. */
data class InspectionSchedule(
    var code: String,
    var title: String,
    var startsAt: Int,
    var source: String,
    var quantity: Int,
    var threshold: Double,
    var discount: Double,
    var odometer: Int,
    var contactEmail: String,
    var status: InspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionScheduleStatus): Boolean {
        when (status) {
            InspectionScheduleStatus.CREATED -> {
                when (next) {
                    InspectionScheduleStatus.RETIRED -> return true
                }
                return false
            }
            InspectionScheduleStatus.VALIDATED -> {
                when (next) {
                    InspectionScheduleStatus.PUBLISHED, InspectionScheduleStatus.RETIRED -> return true
                }
                return false
            }
            InspectionScheduleStatus.PUBLISHED -> {
                when (next) {
                    InspectionScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("inspection schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((threshold + startsAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionSchedule {
        return InspectionSchedule(
            code = code,
            title = title,
            startsAt = startsAt,
            source = source,
            quantity = quantity,
            threshold = threshold,
            discount = discount,
            odometer = odometer,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a inspection schedule in status created. */
fun newInspectionSchedule(code: String, title: String): InspectionSchedule {
    return InspectionSchedule(
        code = code,
        title = title,
        startsAt = 0,
        source = "",
        quantity = 0,
        threshold = 0.0,
        discount = 0.0,
        odometer = 0,
        contactEmail = "",
        status = InspectionScheduleStatus.CREATED,
    )
}
