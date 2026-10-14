package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.text.normalize

/** Where a rolling dwell mapping is in its life cycle. */
enum class RollingDwellMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling dwell mapping and its current status. */
data class RollingDwellMapping(
    var code: String,
    var label: String,
    var contactEmail: String,
    var lane: String,
    var timezone: String,
    var retries: Int,
    var status: RollingDwellMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDwellMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDwellMappingStatus): Boolean {
        when (status) {
            RollingDwellMappingStatus.PLANNED -> {
                when (next) {
                    RollingDwellMappingStatus.SCHEDULED -> return true
                }
                return false
            }
            RollingDwellMappingStatus.SCHEDULED -> return next == RollingDwellMappingStatus.COMPLETED || next == RollingDwellMappingStatus.CANCELLED
            RollingDwellMappingStatus.IN_PROGRESS -> return next == RollingDwellMappingStatus.COMPLETED
            RollingDwellMappingStatus.COMPLETED -> return next == RollingDwellMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingDwellMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling dwell mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling dwell mapping"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): RollingDwellMapping {
        return RollingDwellMapping(
            code = code,
            label = label,
            contactEmail = contactEmail,
            lane = lane,
            timezone = timezone,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling dwell mapping in status planned. */
fun newRollingDwellMapping(code: String, label: String): RollingDwellMapping {
    return RollingDwellMapping(
        code = code,
        label = label,
        contactEmail = "",
        lane = "",
        timezone = "",
        retries = 0,
        status = RollingDwellMappingStatus.PLANNED,
    )
}
