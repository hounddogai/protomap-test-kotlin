package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone schedule is in its life cycle. */
enum class ZoneScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A zone schedule as the zone team keeps it. */
data class ZoneSchedule(
    var code: String,
    var name: String,
    var checksum: String,
    var ownerId: String,
    var timezone: String,
    var summary: String,
    var minimum: Double,
    var pallets: Int,
    var startsAt: Int,
    var stopCount: Int,
    var contactEmail: String,
    var status: ZoneScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneScheduleStatus): Boolean {
        when (status) {
            ZoneScheduleStatus.PROPOSED -> {
                when (next) {
                    ZoneScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            ZoneScheduleStatus.APPROVED -> return next == ZoneScheduleStatus.WITHDRAWN
            ZoneScheduleStatus.REJECTED -> {
                when (next) {
                    ZoneScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneScheduleStatus) {
        if (!canMoveTo(next)) {
            error("zone schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone schedule"} $code"

    fun effectiveExposure(): Double = round((pallets.toDouble() + minimum) * 100) / 100

    fun snapshot(): ZoneSchedule {
        return ZoneSchedule(
            code = code,
            name = name,
            checksum = checksum,
            ownerId = ownerId,
            timezone = timezone,
            summary = summary,
            minimum = minimum,
            pallets = pallets,
            startsAt = startsAt,
            stopCount = stopCount,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a zone schedule in status proposed. */
fun newZoneSchedule(code: String, name: String): ZoneSchedule {
    return ZoneSchedule(
        code = code,
        name = name,
        checksum = "",
        ownerId = "",
        timezone = "",
        summary = "",
        minimum = 0.0,
        pallets = 0,
        startsAt = 0,
        stopCount = 0,
        contactEmail = "",
        status = ZoneScheduleStatus.PROPOSED,
    )
}
