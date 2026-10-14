package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker schedule records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker schedule is in its life cycle. */
enum class LockerScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A locker schedule. Its code is unique within a tenant. */
data class LockerSchedule(
    var code: String,
    var title: String,
    var currency: String,
    var maximum: Double,
    var segment: String,
    var fuelLiters: Double,
    var category: String,
    var margin: Double,
    var status: LockerScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerScheduleStatus): Boolean {
        when (status) {
            LockerScheduleStatus.CREATED -> return next == LockerScheduleStatus.PUBLISHED
            LockerScheduleStatus.VALIDATED -> return next == LockerScheduleStatus.PUBLISHED || next == LockerScheduleStatus.RETIRED
            LockerScheduleStatus.PUBLISHED -> return next == LockerScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LockerScheduleStatus) {
        if (!canMoveTo(next)) {
            error("locker schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker schedule"} $code"

    fun netTotal(): Double = round((margin + fuelLiters) * 100) / 100

    fun snapshot(): LockerSchedule {
        return LockerSchedule(
            code = code,
            title = title,
            currency = currency,
            maximum = maximum,
            segment = segment,
            fuelLiters = fuelLiters,
            category = category,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a locker schedule in status created. */
fun newLockerSchedule(code: String, title: String): LockerSchedule {
    return LockerSchedule(
        code = code,
        title = title,
        currency = "",
        maximum = 0.0,
        segment = "",
        fuelLiters = 0.0,
        category = "",
        margin = 0.0,
        status = LockerScheduleStatus.CREATED,
    )
}
