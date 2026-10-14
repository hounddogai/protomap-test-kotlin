package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default relay schedule records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a default relay schedule is in its life cycle. */
enum class DefaultRelayScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default relay schedule and its current status. */
data class DefaultRelaySchedule(
    var code: String,
    var title: String,
    var loadKg: Double,
    var unit: String,
    var quantity: Int,
    var threshold: Double,
    var locale: String,
    var status: DefaultRelayScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRelayScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRelayScheduleStatus): Boolean {
        when (status) {
            DefaultRelayScheduleStatus.IDLE -> {
                when (next) {
                    DefaultRelayScheduleStatus.WARMING -> return true
                }
                return false
            }
            DefaultRelayScheduleStatus.WARMING -> {
                when (next) {
                    DefaultRelayScheduleStatus.DRAINING, DefaultRelayScheduleStatus.STOPPED -> return true
                }
                return false
            }
            DefaultRelayScheduleStatus.READY -> return next == DefaultRelayScheduleStatus.STOPPED
            DefaultRelayScheduleStatus.DRAINING -> {
                when (next) {
                    DefaultRelayScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRelayScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default relay schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((loadKg - quantity.toDouble()) * 100) / 100

    fun snapshot(): DefaultRelaySchedule {
        return DefaultRelaySchedule(
            code = code,
            title = title,
            loadKg = loadKg,
            unit = unit,
            quantity = quantity,
            threshold = threshold,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default relay schedule in status idle. */
fun newDefaultRelaySchedule(code: String, title: String): DefaultRelaySchedule {
    return DefaultRelaySchedule(
        code = code,
        title = title,
        loadKg = 0.0,
        unit = "",
        quantity = 0,
        threshold = 0.0,
        locale = "",
        status = DefaultRelayScheduleStatus.IDLE,
    )
}
