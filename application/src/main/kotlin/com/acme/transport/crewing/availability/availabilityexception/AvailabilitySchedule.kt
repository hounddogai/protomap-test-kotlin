package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize

/** Where a availability schedule is in its life cycle. */
enum class AvailabilityScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a availability schedule and its current status. */
data class AvailabilitySchedule(
    var code: String,
    var name: String,
    var shiftCode: String,
    var title: String,
    var expiresAt: Int,
    var label: String,
    var currency: String,
    var approved: Boolean,
    var summary: String,
    var status: AvailabilityScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityScheduleStatus): Boolean {
        when (status) {
            AvailabilityScheduleStatus.IDLE -> {
                when (next) {
                    AvailabilityScheduleStatus.STOPPED -> return true
                }
                return false
            }
            AvailabilityScheduleStatus.WARMING -> {
                when (next) {
                    AvailabilityScheduleStatus.READY -> return true
                }
                return false
            }
            AvailabilityScheduleStatus.READY -> {
                when (next) {
                    AvailabilityScheduleStatus.DRAINING, AvailabilityScheduleStatus.STOPPED -> return true
                }
                return false
            }
            AvailabilityScheduleStatus.DRAINING -> {
                when (next) {
                    AvailabilityScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityScheduleStatus) {
        if (!canMoveTo(next)) {
            error("availability schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): AvailabilitySchedule {
        return AvailabilitySchedule(
            code = code,
            name = name,
            shiftCode = shiftCode,
            title = title,
            expiresAt = expiresAt,
            label = label,
            currency = currency,
            approved = approved,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability schedule in status idle. */
fun newAvailabilitySchedule(code: String, name: String): AvailabilitySchedule {
    return AvailabilitySchedule(
        code = code,
        name = name,
        shiftCode = "",
        title = "",
        expiresAt = 0,
        label = "",
        currency = "",
        approved = false,
        summary = "",
        status = AvailabilityScheduleStatus.IDLE,
    )
}
