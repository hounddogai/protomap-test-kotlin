package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled rest break digest is in its life cycle. */
enum class ScheduledRestBreakDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled rest break digest as the rest break team keeps it. */
data class ScheduledRestBreakDigest(
    var code: String,
    var title: String,
    var dock: String,
    var carrier: String,
    var currency: String,
    var durationMinutes: Int,
    var driverId: String,
    var endsAt: Int,
    var status: ScheduledRestBreakDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakDigestStatus): Boolean {
        when (status) {
            ScheduledRestBreakDigestStatus.PLANNED -> {
                when (next) {
                    ScheduledRestBreakDigestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledRestBreakDigestStatus.SCHEDULED -> return next == ScheduledRestBreakDigestStatus.COMPLETED || next == ScheduledRestBreakDigestStatus.CANCELLED
            ScheduledRestBreakDigestStatus.IN_PROGRESS -> return next == ScheduledRestBreakDigestStatus.COMPLETED || next == ScheduledRestBreakDigestStatus.CANCELLED
            ScheduledRestBreakDigestStatus.COMPLETED -> {
                when (next) {
                    ScheduledRestBreakDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled rest break digest"} $code"

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledRestBreakDigest {
        return ScheduledRestBreakDigest(
            code = code,
            title = title,
            dock = dock,
            carrier = carrier,
            currency = currency,
            durationMinutes = durationMinutes,
            driverId = driverId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled rest break digest in status planned. */
fun newScheduledRestBreakDigest(code: String, title: String): ScheduledRestBreakDigest {
    return ScheduledRestBreakDigest(
        code = code,
        title = title,
        dock = "",
        carrier = "",
        currency = "",
        durationMinutes = 0,
        driverId = "",
        endsAt = 0,
        status = ScheduledRestBreakDigestStatus.PLANNED,
    )
}
