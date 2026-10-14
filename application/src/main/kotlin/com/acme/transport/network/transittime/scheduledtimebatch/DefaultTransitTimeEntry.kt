package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default transit time entry is in its life cycle. */
enum class DefaultTransitTimeEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default transit time entry and its current status. */
data class DefaultTransitTimeEntry(
    var code: String,
    var title: String,
    var visible: Boolean,
    var startsAt: Int,
    var minimum: Double,
    var lane: String,
    var status: DefaultTransitTimeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimeEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimeEntryStatus): Boolean {
        when (status) {
            DefaultTransitTimeEntryStatus.NEW -> {
                when (next) {
                    DefaultTransitTimeEntryStatus.QUEUED, DefaultTransitTimeEntryStatus.DONE -> return true
                }
                return false
            }
            DefaultTransitTimeEntryStatus.QUEUED -> return next == DefaultTransitTimeEntryStatus.RUNNING || next == DefaultTransitTimeEntryStatus.DONE
            DefaultTransitTimeEntryStatus.RUNNING -> return next == DefaultTransitTimeEntryStatus.DONE
            DefaultTransitTimeEntryStatus.DONE -> return next == DefaultTransitTimeEntryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimeEntryStatus) {
        if (!canMoveTo(next)) {
            error("default transit time entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default transit time entry"} $code"

    fun effectiveExposure(): Double = round(startsAt.toDouble() * minimum * 100) / 100

    fun snapshot(): DefaultTransitTimeEntry {
        return DefaultTransitTimeEntry(
            code = code,
            title = title,
            visible = visible,
            startsAt = startsAt,
            minimum = minimum,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default transit time entry in status new. */
fun newDefaultTransitTimeEntry(code: String, title: String): DefaultTransitTimeEntry {
    return DefaultTransitTimeEntry(
        code = code,
        title = title,
        visible = false,
        startsAt = 0,
        minimum = 0.0,
        lane = "",
        status = DefaultTransitTimeEntryStatus.NEW,
    )
}
