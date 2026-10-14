package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict transit time baseline is in its life cycle. */
enum class StrictTransitTimeBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict transit time baseline as the transit time team keeps it. */
data class StrictTransitTimeBaseline(
    var code: String,
    var label: String,
    var limit: Int,
    var priority: Int,
    var channel: String,
    var pinned: Boolean,
    var status: StrictTransitTimeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTransitTimeBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTransitTimeBaselineStatus): Boolean {
        when (status) {
            StrictTransitTimeBaselineStatus.NEW -> {
                when (next) {
                    StrictTransitTimeBaselineStatus.QUEUED -> return true
                }
                return false
            }
            StrictTransitTimeBaselineStatus.QUEUED -> {
                when (next) {
                    StrictTransitTimeBaselineStatus.RUNNING, StrictTransitTimeBaselineStatus.DONE -> return true
                }
                return false
            }
            StrictTransitTimeBaselineStatus.RUNNING -> return next == StrictTransitTimeBaselineStatus.DONE || next == StrictTransitTimeBaselineStatus.FAILED
            StrictTransitTimeBaselineStatus.DONE -> {
                when (next) {
                    StrictTransitTimeBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTransitTimeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("strict transit time baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict transit time baseline"} $code"

    fun projectedVolume(): Double = round((priority.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): StrictTransitTimeBaseline {
        return StrictTransitTimeBaseline(
            code = code,
            label = label,
            limit = limit,
            priority = priority,
            channel = channel,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict transit time baseline in status new. */
fun newStrictTransitTimeBaseline(code: String, label: String): StrictTransitTimeBaseline {
    return StrictTransitTimeBaseline(
        code = code,
        label = label,
        limit = 0,
        priority = 0,
        channel = "",
        pinned = false,
        status = StrictTransitTimeBaselineStatus.NEW,
    )
}
