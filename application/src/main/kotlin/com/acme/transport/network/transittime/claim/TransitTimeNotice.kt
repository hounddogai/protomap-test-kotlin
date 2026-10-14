package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time notice is in its life cycle. */
enum class TransitTimeNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One transit time notice, with what the planners need to act on it. */
data class TransitTimeNotice(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var driverId: String,
    var status: TransitTimeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeNoticeStatus): Boolean {
        when (status) {
            TransitTimeNoticeStatus.PLANNED -> {
                when (next) {
                    TransitTimeNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            TransitTimeNoticeStatus.SCHEDULED -> {
                when (next) {
                    TransitTimeNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            TransitTimeNoticeStatus.IN_PROGRESS -> return next == TransitTimeNoticeStatus.COMPLETED || next == TransitTimeNoticeStatus.CANCELLED
            TransitTimeNoticeStatus.COMPLETED -> return next == TransitTimeNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TransitTimeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("transit time notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round((createdAt.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeNotice {
        return TransitTimeNotice(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time notice in status planned. */
fun newTransitTimeNotice(code: String, title: String): TransitTimeNotice {
    return TransitTimeNotice(
        code = code,
        title = title,
        durationMinutes = 0,
        createdAt = 0,
        driverId = "",
        status = TransitTimeNoticeStatus.PLANNED,
    )
}
