package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rest break window is in its life cycle. */
enum class GlobalRestBreakWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a global rest break window and its current status. */
data class GlobalRestBreakWindow(
    var code: String,
    var title: String,
    var name: String,
    var margin: Double,
    var windowDays: Int,
    var cost: Double,
    var fuelLiters: Double,
    var startsAt: Int,
    var rate: Double,
    var active: Boolean,
    var tags: MutableList<String>,
    var status: GlobalRestBreakWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRestBreakWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRestBreakWindowStatus): Boolean {
        when (status) {
            GlobalRestBreakWindowStatus.IDLE -> return next == GlobalRestBreakWindowStatus.STOPPED
            GlobalRestBreakWindowStatus.WARMING -> {
                when (next) {
                    GlobalRestBreakWindowStatus.READY -> return true
                }
                return false
            }
            GlobalRestBreakWindowStatus.READY -> return next == GlobalRestBreakWindowStatus.STOPPED
            GlobalRestBreakWindowStatus.DRAINING -> {
                when (next) {
                    GlobalRestBreakWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRestBreakWindowStatus) {
        if (!canMoveTo(next)) {
            error("global rest break window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global rest break window"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((margin - cost) * 100) / 100

    fun snapshot(): GlobalRestBreakWindow {
        return GlobalRestBreakWindow(
            code = code,
            title = title,
            name = name,
            margin = margin,
            windowDays = windowDays,
            cost = cost,
            fuelLiters = fuelLiters,
            startsAt = startsAt,
            rate = rate,
            active = active,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global rest break window in status idle. */
fun newGlobalRestBreakWindow(code: String, title: String): GlobalRestBreakWindow {
    return GlobalRestBreakWindow(
        code = code,
        title = title,
        name = "",
        margin = 0.0,
        windowDays = 0,
        cost = 0.0,
        fuelLiters = 0.0,
        startsAt = 0,
        rate = 0.0,
        active = false,
        tags = mutableListOf<String>(),
        status = GlobalRestBreakWindowStatus.IDLE,
    )
}
