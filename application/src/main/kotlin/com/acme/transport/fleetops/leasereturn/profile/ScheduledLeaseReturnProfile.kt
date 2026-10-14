package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled lease return profile is in its life cycle. */
enum class ScheduledLeaseReturnProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled lease return profile, with what the planners need to act on it. */
data class ScheduledLeaseReturnProfile(
    var code: String,
    var title: String,
    var externalRef: String,
    var floorValue: Double,
    var carrier: String,
    var summary: String,
    var shiftCode: String,
    var retries: Int,
    var segment: String,
    var margin: Double,
    var priority: Int,
    var status: ScheduledLeaseReturnProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLeaseReturnProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLeaseReturnProfileStatus): Boolean {
        when (status) {
            ScheduledLeaseReturnProfileStatus.IDLE -> {
                when (next) {
                    ScheduledLeaseReturnProfileStatus.READY -> return true
                }
                return false
            }
            ScheduledLeaseReturnProfileStatus.WARMING -> return next == ScheduledLeaseReturnProfileStatus.READY || next == ScheduledLeaseReturnProfileStatus.DRAINING
            ScheduledLeaseReturnProfileStatus.READY -> return next == ScheduledLeaseReturnProfileStatus.DRAINING || next == ScheduledLeaseReturnProfileStatus.STOPPED
            ScheduledLeaseReturnProfileStatus.DRAINING -> return next == ScheduledLeaseReturnProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledLeaseReturnProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lease return profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round(priority.toDouble() * floorValue * 100) / 100

    fun snapshot(): ScheduledLeaseReturnProfile {
        return ScheduledLeaseReturnProfile(
            code = code,
            title = title,
            externalRef = externalRef,
            floorValue = floorValue,
            carrier = carrier,
            summary = summary,
            shiftCode = shiftCode,
            retries = retries,
            segment = segment,
            margin = margin,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled lease return profile in status idle. */
fun newScheduledLeaseReturnProfile(code: String, title: String): ScheduledLeaseReturnProfile {
    return ScheduledLeaseReturnProfile(
        code = code,
        title = title,
        externalRef = "",
        floorValue = 0.0,
        carrier = "",
        summary = "",
        shiftCode = "",
        retries = 0,
        segment = "",
        margin = 0.0,
        priority = 0,
        status = ScheduledLeaseReturnProfileStatus.IDLE,
    )
}
