package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled redelivery notice is in its life cycle. */
enum class ScheduledRedeliveryNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled redelivery notice, with what the planners need to act on it. */
data class ScheduledRedeliveryNotice(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var driverId: String,
    var country: String,
    var status: ScheduledRedeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryNoticeStatus): Boolean {
        when (status) {
            ScheduledRedeliveryNoticeStatus.OPEN -> return next == ScheduledRedeliveryNoticeStatus.RESOLVED
            ScheduledRedeliveryNoticeStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledRedeliveryNoticeStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledRedeliveryNoticeStatus.RESOLVED -> {
                when (next) {
                    ScheduledRedeliveryNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledRedeliveryNoticeStatus.REOPENED -> return next == ScheduledRedeliveryNoticeStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled redelivery notice"} $code"

    fun projectedVolume(): Double = round((etaMinutes.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRedeliveryNotice {
        return ScheduledRedeliveryNotice(
            code = code,
            title = title,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            driverId = driverId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled redelivery notice in status open. */
fun newScheduledRedeliveryNotice(code: String, title: String): ScheduledRedeliveryNotice {
    return ScheduledRedeliveryNotice(
        code = code,
        title = title,
        expiresAt = 0,
        etaMinutes = 0,
        driverId = "",
        country = "",
        status = ScheduledRedeliveryNoticeStatus.OPEN,
    )
}
