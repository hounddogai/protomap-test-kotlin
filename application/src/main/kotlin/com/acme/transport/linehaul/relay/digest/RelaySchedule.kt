package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay schedule is in its life cycle. */
enum class RelayScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a relay schedule and its current status. */
data class RelaySchedule(
    var code: String,
    var label: String,
    var stopCount: Int,
    var capacity: Int,
    var timezone: String,
    var status: RelayScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayScheduleStatus): Boolean {
        when (status) {
            RelayScheduleStatus.REQUESTED -> {
                when (next) {
                    RelayScheduleStatus.GRANTED -> return true
                }
                return false
            }
            RelayScheduleStatus.GRANTED -> {
                when (next) {
                    RelayScheduleStatus.REVOKED -> return true
                }
                return false
            }
            RelayScheduleStatus.REVOKED -> {
                when (next) {
                    RelayScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayScheduleStatus) {
        if (!canMoveTo(next)) {
            error("relay schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((capacity.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RelaySchedule {
        return RelaySchedule(
            code = code,
            label = label,
            stopCount = stopCount,
            capacity = capacity,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a relay schedule in status requested. */
fun newRelaySchedule(code: String, label: String): RelaySchedule {
    return RelaySchedule(
        code = code,
        label = label,
        stopCount = 0,
        capacity = 0,
        timezone = "",
        status = RelayScheduleStatus.REQUESTED,
    )
}
