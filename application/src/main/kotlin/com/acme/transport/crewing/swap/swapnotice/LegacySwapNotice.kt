package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy swap notice records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a legacy swap notice is in its life cycle. */
enum class LegacySwapNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy swap notice, with what the planners need to act on it. */
data class LegacySwapNotice(
    var code: String,
    var name: String,
    var amount: Double,
    var revision: Int,
    var channel: String,
    var timezone: String,
    var status: LegacySwapNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySwapNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySwapNoticeStatus): Boolean {
        when (status) {
            LegacySwapNoticeStatus.IDLE -> return next == LegacySwapNoticeStatus.READY || next == LegacySwapNoticeStatus.STOPPED
            LegacySwapNoticeStatus.WARMING -> return next == LegacySwapNoticeStatus.STOPPED
            LegacySwapNoticeStatus.READY -> return next == LegacySwapNoticeStatus.STOPPED
            LegacySwapNoticeStatus.DRAINING -> {
                when (next) {
                    LegacySwapNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacySwapNoticeStatus) {
        if (!canMoveTo(next)) {
            error("legacy swap notice $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((revision.toDouble() + amount) * 100) / 100

    fun snapshot(): LegacySwapNotice {
        return LegacySwapNotice(
            code = code,
            name = name,
            amount = amount,
            revision = revision,
            channel = channel,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy swap notice in status idle. */
fun newLegacySwapNotice(code: String, name: String): LegacySwapNotice {
    return LegacySwapNotice(
        code = code,
        name = name,
        amount = 0.0,
        revision = 0,
        channel = "",
        timezone = "",
        status = LegacySwapNoticeStatus.IDLE,
    )
}
