package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dock door notice is in its life cycle. */
enum class ScheduledDockDoorNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled dock door notice and its current status. */
data class ScheduledDockDoorNotice(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var trailerId: String,
    var pallets: Int,
    var windowDays: Int,
    var score: Double,
    var locale: String,
    var currency: String,
    var region: String,
    var maximum: Double,
    var status: ScheduledDockDoorNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorNoticeStatus): Boolean {
        when (status) {
            ScheduledDockDoorNoticeStatus.IDLE -> return next == ScheduledDockDoorNoticeStatus.WARMING || next == ScheduledDockDoorNoticeStatus.STOPPED
            ScheduledDockDoorNoticeStatus.WARMING -> {
                when (next) {
                    ScheduledDockDoorNoticeStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledDockDoorNoticeStatus.READY -> return next == ScheduledDockDoorNoticeStatus.STOPPED
            ScheduledDockDoorNoticeStatus.DRAINING -> return next == ScheduledDockDoorNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((windowDays.toDouble() - maximum) * 100) / 100

    fun snapshot(): ScheduledDockDoorNotice {
        return ScheduledDockDoorNotice(
            code = code,
            title = title,
            pinned = pinned,
            trailerId = trailerId,
            pallets = pallets,
            windowDays = windowDays,
            score = score,
            locale = locale,
            currency = currency,
            region = region,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled dock door notice in status idle. */
fun newScheduledDockDoorNotice(code: String, title: String): ScheduledDockDoorNotice {
    return ScheduledDockDoorNotice(
        code = code,
        title = title,
        pinned = false,
        trailerId = "",
        pallets = 0,
        windowDays = 0,
        score = 0.0,
        locale = "",
        currency = "",
        region = "",
        maximum = 0.0,
        status = ScheduledDockDoorNoticeStatus.IDLE,
    )
}
