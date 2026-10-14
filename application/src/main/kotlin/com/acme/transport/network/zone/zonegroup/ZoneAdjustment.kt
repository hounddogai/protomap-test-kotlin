package com.acme.transport.network.zone.zonegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone adjustment is in its life cycle. */
enum class ZoneAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A zone adjustment as the zone team keeps it. */
data class ZoneAdjustment(
    var code: String,
    var title: String,
    var endsAt: Int,
    var startsAt: Int,
    var active: Boolean,
    var tags: MutableList<String>,
    var shiftCode: String,
    var lane: String,
    var pinned: Boolean,
    var rate: Double,
    var distanceKm: Double,
    var status: ZoneAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneAdjustmentStatus): Boolean {
        when (status) {
            ZoneAdjustmentStatus.IDLE -> return next == ZoneAdjustmentStatus.WARMING || next == ZoneAdjustmentStatus.DRAINING
            ZoneAdjustmentStatus.WARMING -> return next == ZoneAdjustmentStatus.READY || next == ZoneAdjustmentStatus.STOPPED
            ZoneAdjustmentStatus.READY -> {
                when (next) {
                    ZoneAdjustmentStatus.DRAINING, ZoneAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            ZoneAdjustmentStatus.DRAINING -> return next == ZoneAdjustmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ZoneAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("zone adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round(rate * endsAt.toDouble() * 100) / 100

    fun snapshot(): ZoneAdjustment {
        return ZoneAdjustment(
            code = code,
            title = title,
            endsAt = endsAt,
            startsAt = startsAt,
            active = active,
            tags = tags,
            shiftCode = shiftCode,
            lane = lane,
            pinned = pinned,
            rate = rate,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a zone adjustment in status idle. */
fun newZoneAdjustment(code: String, title: String): ZoneAdjustment {
    return ZoneAdjustment(
        code = code,
        title = title,
        endsAt = 0,
        startsAt = 0,
        active = false,
        tags = mutableListOf<String>(),
        shiftCode = "",
        lane = "",
        pinned = false,
        rate = 0.0,
        distanceKm = 0.0,
        status = ZoneAdjustmentStatus.IDLE,
    )
}
