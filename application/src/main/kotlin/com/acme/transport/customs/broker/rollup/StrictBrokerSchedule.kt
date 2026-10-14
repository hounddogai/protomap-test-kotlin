package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict broker schedule is in its life cycle. */
enum class StrictBrokerScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict broker schedule and its current status. */
data class StrictBrokerSchedule(
    var code: String,
    var name: String,
    var category: String,
    var segment: String,
    var createdAt: Int,
    var discount: Double,
    var updatedAt: Int,
    var expiresAt: Int,
    var driverId: String,
    var limit: Int,
    var status: StrictBrokerScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBrokerScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBrokerScheduleStatus): Boolean {
        when (status) {
            StrictBrokerScheduleStatus.IDLE -> return next == StrictBrokerScheduleStatus.DRAINING
            StrictBrokerScheduleStatus.WARMING -> return next == StrictBrokerScheduleStatus.DRAINING || next == StrictBrokerScheduleStatus.STOPPED
            StrictBrokerScheduleStatus.READY -> {
                when (next) {
                    StrictBrokerScheduleStatus.STOPPED -> return true
                }
                return false
            }
            StrictBrokerScheduleStatus.DRAINING -> {
                when (next) {
                    StrictBrokerScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictBrokerScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict broker schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round(discount * expiresAt.toDouble() * 100) / 100

    fun snapshot(): StrictBrokerSchedule {
        return StrictBrokerSchedule(
            code = code,
            name = name,
            category = category,
            segment = segment,
            createdAt = createdAt,
            discount = discount,
            updatedAt = updatedAt,
            expiresAt = expiresAt,
            driverId = driverId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict broker schedule in status idle. */
fun newStrictBrokerSchedule(code: String, name: String): StrictBrokerSchedule {
    return StrictBrokerSchedule(
        code = code,
        name = name,
        category = "",
        segment = "",
        createdAt = 0,
        discount = 0.0,
        updatedAt = 0,
        expiresAt = 0,
        driverId = "",
        limit = 0,
        status = StrictBrokerScheduleStatus.IDLE,
    )
}
