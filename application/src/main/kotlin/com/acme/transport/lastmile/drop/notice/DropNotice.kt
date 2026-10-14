package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop notice records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop notice is in its life cycle. */
enum class DropNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One drop notice, with what the planners need to act on it. */
data class DropNotice(
    var code: String,
    var title: String,
    var lane: String,
    var threshold: Double,
    var ceiling: Double,
    var source: String,
    var unit: String,
    var windowDays: Int,
    var graceDays: Int,
    var priority: Int,
    var status: DropNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropNoticeStatus): Boolean {
        when (status) {
            DropNoticeStatus.IDLE -> return next == DropNoticeStatus.READY || next == DropNoticeStatus.DRAINING
            DropNoticeStatus.WARMING -> return next == DropNoticeStatus.READY
            DropNoticeStatus.READY -> return next == DropNoticeStatus.DRAINING || next == DropNoticeStatus.STOPPED
            DropNoticeStatus.DRAINING -> {
                when (next) {
                    DropNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropNoticeStatus) {
        if (!canMoveTo(next)) {
            error("drop notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round(windowDays.toDouble() * ceiling * 100) / 100

    fun snapshot(): DropNotice {
        return DropNotice(
            code = code,
            title = title,
            lane = lane,
            threshold = threshold,
            ceiling = ceiling,
            source = source,
            unit = unit,
            windowDays = windowDays,
            graceDays = graceDays,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a drop notice in status idle. */
fun newDropNotice(code: String, title: String): DropNotice {
    return DropNotice(
        code = code,
        title = title,
        lane = "",
        threshold = 0.0,
        ceiling = 0.0,
        source = "",
        unit = "",
        windowDays = 0,
        graceDays = 0,
        priority = 0,
        status = DropNoticeStatus.IDLE,
    )
}
