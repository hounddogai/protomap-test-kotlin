package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone target is in its life cycle. */
enum class ZoneTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A zone target as the zone team keeps it. */
data class ZoneTarget(
    var code: String,
    var title: String,
    var ownerId: String,
    var active: Boolean,
    var durationMinutes: Int,
    var startsAt: Int,
    var ceiling: Double,
    var discount: Double,
    var dock: String,
    var carrier: String,
    var status: ZoneTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneTargetStatus): Boolean {
        when (status) {
            ZoneTargetStatus.IDLE -> return next == ZoneTargetStatus.STOPPED
            ZoneTargetStatus.WARMING -> {
                when (next) {
                    ZoneTargetStatus.READY, ZoneTargetStatus.STOPPED -> return true
                }
                return false
            }
            ZoneTargetStatus.READY -> return next == ZoneTargetStatus.DRAINING || next == ZoneTargetStatus.STOPPED
            ZoneTargetStatus.DRAINING -> return next == ZoneTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ZoneTargetStatus) {
        if (!canMoveTo(next)) {
            error("zone target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone target"} $code"

    fun adjustedVolume(): Double = round(durationMinutes.toDouble() * discount * 100) / 100

    fun snapshot(): ZoneTarget {
        return ZoneTarget(
            code = code,
            title = title,
            ownerId = ownerId,
            active = active,
            durationMinutes = durationMinutes,
            startsAt = startsAt,
            ceiling = ceiling,
            discount = discount,
            dock = dock,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a zone target in status idle. */
fun newZoneTarget(code: String, title: String): ZoneTarget {
    return ZoneTarget(
        code = code,
        title = title,
        ownerId = "",
        active = false,
        durationMinutes = 0,
        startsAt = 0,
        ceiling = 0.0,
        discount = 0.0,
        dock = "",
        carrier = "",
        status = ZoneTargetStatus.IDLE,
    )
}
