package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling drop notice records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a rolling drop notice is in its life cycle. */
enum class RollingDropNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling drop notice as the drop team keeps it. */
data class RollingDropNotice(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var timezone: String,
    var floorValue: Double,
    var status: RollingDropNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDropNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDropNoticeStatus): Boolean {
        when (status) {
            RollingDropNoticeStatus.IDLE -> {
                when (next) {
                    RollingDropNoticeStatus.WARMING -> return true
                }
                return false
            }
            RollingDropNoticeStatus.WARMING -> return next == RollingDropNoticeStatus.STOPPED
            RollingDropNoticeStatus.READY -> return next == RollingDropNoticeStatus.STOPPED
            RollingDropNoticeStatus.DRAINING -> {
                when (next) {
                    RollingDropNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDropNoticeStatus) {
        if (!canMoveTo(next)) {
            error("rolling drop notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling drop notice"} $code"

    fun projectedValue(): Double = round((expiresAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): RollingDropNotice {
        return RollingDropNotice(
            code = code,
            label = label,
            expiresAt = expiresAt,
            timezone = timezone,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling drop notice in status idle. */
fun newRollingDropNotice(code: String, label: String): RollingDropNotice {
    return RollingDropNotice(
        code = code,
        label = label,
        expiresAt = 0,
        timezone = "",
        floorValue = 0.0,
        status = RollingDropNoticeStatus.IDLE,
    )
}
