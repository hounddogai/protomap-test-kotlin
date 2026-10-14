package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled broker score is in its life cycle. */
enum class ScheduledBrokerScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled broker score and its current status. */
data class ScheduledBrokerScore(
    var code: String,
    var label: String,
    var externalRef: String,
    var capacity: Int,
    var shiftCode: String,
    var trailerId: String,
    var unit: String,
    var driverId: String,
    var region: String,
    var stopCount: Int,
    var ratio: Double,
    var status: ScheduledBrokerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBrokerScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBrokerScoreStatus): Boolean {
        when (status) {
            ScheduledBrokerScoreStatus.IDLE -> {
                when (next) {
                    ScheduledBrokerScoreStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledBrokerScoreStatus.WARMING -> {
                when (next) {
                    ScheduledBrokerScoreStatus.READY, ScheduledBrokerScoreStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledBrokerScoreStatus.READY -> {
                when (next) {
                    ScheduledBrokerScoreStatus.DRAINING, ScheduledBrokerScoreStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledBrokerScoreStatus.DRAINING -> return next == ScheduledBrokerScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledBrokerScoreStatus) {
        if (!canMoveTo(next)) {
            error("scheduled broker score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled broker score"} $code"

    fun effectiveValue(): Double = round((ratio + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ScheduledBrokerScore {
        return ScheduledBrokerScore(
            code = code,
            label = label,
            externalRef = externalRef,
            capacity = capacity,
            shiftCode = shiftCode,
            trailerId = trailerId,
            unit = unit,
            driverId = driverId,
            region = region,
            stopCount = stopCount,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled broker score in status idle. */
fun newScheduledBrokerScore(code: String, label: String): ScheduledBrokerScore {
    return ScheduledBrokerScore(
        code = code,
        label = label,
        externalRef = "",
        capacity = 0,
        shiftCode = "",
        trailerId = "",
        unit = "",
        driverId = "",
        region = "",
        stopCount = 0,
        ratio = 0.0,
        status = ScheduledBrokerScoreStatus.IDLE,
    )
}
