package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rest break notice is in its life cycle. */
enum class CappedRestBreakNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped rest break notice and its current status. */
data class CappedRestBreakNotice(
    var code: String,
    var name: String,
    var channel: String,
    var createdAt: Int,
    var odometer: Int,
    var ceiling: Double,
    var status: CappedRestBreakNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRestBreakNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRestBreakNoticeStatus): Boolean {
        when (status) {
            CappedRestBreakNoticeStatus.REQUESTED -> return next == CappedRestBreakNoticeStatus.EXPIRED
            CappedRestBreakNoticeStatus.GRANTED -> {
                when (next) {
                    CappedRestBreakNoticeStatus.REVOKED -> return true
                }
                return false
            }
            CappedRestBreakNoticeStatus.REVOKED -> return next == CappedRestBreakNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedRestBreakNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped rest break notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((createdAt.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): CappedRestBreakNotice {
        return CappedRestBreakNotice(
            code = code,
            name = name,
            channel = channel,
            createdAt = createdAt,
            odometer = odometer,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped rest break notice in status requested. */
fun newCappedRestBreakNotice(code: String, name: String): CappedRestBreakNotice {
    return CappedRestBreakNotice(
        code = code,
        name = name,
        channel = "",
        createdAt = 0,
        odometer = 0,
        ceiling = 0.0,
        status = CappedRestBreakNoticeStatus.REQUESTED,
    )
}
