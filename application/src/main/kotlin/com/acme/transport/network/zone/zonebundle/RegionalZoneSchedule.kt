package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional zone schedule is in its life cycle. */
enum class RegionalZoneScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional zone schedule and its current status. */
data class RegionalZoneSchedule(
    var code: String,
    var title: String,
    var locale: String,
    var loadKg: Double,
    var amount: Double,
    var windowDays: Int,
    var country: String,
    var carrier: String,
    var maximum: Double,
    var status: RegionalZoneScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalZoneScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalZoneScheduleStatus): Boolean {
        when (status) {
            RegionalZoneScheduleStatus.PROPOSED -> {
                when (next) {
                    RegionalZoneScheduleStatus.APPROVED -> return true
                }
                return false
            }
            RegionalZoneScheduleStatus.APPROVED -> {
                when (next) {
                    RegionalZoneScheduleStatus.REJECTED, RegionalZoneScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalZoneScheduleStatus.REJECTED -> return next == RegionalZoneScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalZoneScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional zone schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((windowDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): RegionalZoneSchedule {
        return RegionalZoneSchedule(
            code = code,
            title = title,
            locale = locale,
            loadKg = loadKg,
            amount = amount,
            windowDays = windowDays,
            country = country,
            carrier = carrier,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional zone schedule in status proposed. */
fun newRegionalZoneSchedule(code: String, title: String): RegionalZoneSchedule {
    return RegionalZoneSchedule(
        code = code,
        title = title,
        locale = "",
        loadKg = 0.0,
        amount = 0.0,
        windowDays = 0,
        country = "",
        carrier = "",
        maximum = 0.0,
        status = RegionalZoneScheduleStatus.PROPOSED,
    )
}
