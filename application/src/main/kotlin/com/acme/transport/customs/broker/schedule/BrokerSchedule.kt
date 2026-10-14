package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker schedule records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker schedule is in its life cycle. */
enum class BrokerScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a broker schedule and its current status. */
data class BrokerSchedule(
    var code: String,
    var title: String,
    var region: String,
    var timezone: String,
    var graceDays: Int,
    var segment: String,
    var odometer: Int,
    var status: BrokerScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerScheduleStatus): Boolean {
        when (status) {
            BrokerScheduleStatus.REQUESTED -> return next == BrokerScheduleStatus.REVOKED
            BrokerScheduleStatus.GRANTED -> {
                when (next) {
                    BrokerScheduleStatus.REVOKED, BrokerScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            BrokerScheduleStatus.REVOKED -> return next == BrokerScheduleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BrokerScheduleStatus) {
        if (!canMoveTo(next)) {
            error("broker schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((graceDays.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): BrokerSchedule {
        return BrokerSchedule(
            code = code,
            title = title,
            region = region,
            timezone = timezone,
            graceDays = graceDays,
            segment = segment,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a broker schedule in status requested. */
fun newBrokerSchedule(code: String, title: String): BrokerSchedule {
    return BrokerSchedule(
        code = code,
        title = title,
        region = "",
        timezone = "",
        graceDays = 0,
        segment = "",
        odometer = 0,
        status = BrokerScheduleStatus.REQUESTED,
    )
}
