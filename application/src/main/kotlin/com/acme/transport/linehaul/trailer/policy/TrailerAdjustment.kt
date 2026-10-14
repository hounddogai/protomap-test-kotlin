package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.text.normalize

/** Where a trailer adjustment is in its life cycle. */
enum class TrailerAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One trailer adjustment, with what the planners need to act on it. */
data class TrailerAdjustment(
    var code: String,
    var title: String,
    var region: String,
    var summary: String,
    var windowDays: Int,
    var status: TrailerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerAdjustmentStatus): Boolean {
        when (status) {
            TrailerAdjustmentStatus.NEW -> {
                when (next) {
                    TrailerAdjustmentStatus.QUEUED -> return true
                }
                return false
            }
            TrailerAdjustmentStatus.QUEUED -> return next == TrailerAdjustmentStatus.DONE
            TrailerAdjustmentStatus.RUNNING -> return next == TrailerAdjustmentStatus.DONE
            TrailerAdjustmentStatus.DONE -> return next == TrailerAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TrailerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("trailer adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): TrailerAdjustment {
        return TrailerAdjustment(
            code = code,
            title = title,
            region = region,
            summary = summary,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a trailer adjustment in status new. */
fun newTrailerAdjustment(code: String, title: String): TrailerAdjustment {
    return TrailerAdjustment(
        code = code,
        title = title,
        region = "",
        summary = "",
        windowDays = 0,
        status = TrailerAdjustmentStatus.NEW,
    )
}
