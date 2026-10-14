package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default route offset baseline is in its life cycle. */
enum class DefaultRouteOffsetBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default route offset baseline, with what the planners need to act on it. */
data class DefaultRouteOffsetBaseline(
    var code: String,
    var label: String,
    var description: String,
    var durationMinutes: Int,
    var title: String,
    var sequence: Int,
    var notes: String,
    var status: DefaultRouteOffsetBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRouteOffsetBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRouteOffsetBaselineStatus): Boolean {
        when (status) {
            DefaultRouteOffsetBaselineStatus.IDLE -> return next == DefaultRouteOffsetBaselineStatus.WARMING || next == DefaultRouteOffsetBaselineStatus.DRAINING
            DefaultRouteOffsetBaselineStatus.WARMING -> {
                when (next) {
                    DefaultRouteOffsetBaselineStatus.READY, DefaultRouteOffsetBaselineStatus.STOPPED -> return true
                }
                return false
            }
            DefaultRouteOffsetBaselineStatus.READY -> {
                when (next) {
                    DefaultRouteOffsetBaselineStatus.DRAINING, DefaultRouteOffsetBaselineStatus.STOPPED -> return true
                }
                return false
            }
            DefaultRouteOffsetBaselineStatus.DRAINING -> return next == DefaultRouteOffsetBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultRouteOffsetBaselineStatus) {
        if (!canMoveTo(next)) {
            error("default route offset baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round((sequence.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultRouteOffsetBaseline {
        return DefaultRouteOffsetBaseline(
            code = code,
            label = label,
            description = description,
            durationMinutes = durationMinutes,
            title = title,
            sequence = sequence,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default route offset baseline in status idle. */
fun newDefaultRouteOffsetBaseline(code: String, label: String): DefaultRouteOffsetBaseline {
    return DefaultRouteOffsetBaseline(
        code = code,
        label = label,
        description = "",
        durationMinutes = 0,
        title = "",
        sequence = 0,
        notes = "",
        status = DefaultRouteOffsetBaselineStatus.IDLE,
    )
}
