package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled transit time entry is in its life cycle. */
enum class ScheduledTransitTimeEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled transit time entry as the transit time team keeps it. */
data class ScheduledTransitTimeEntry(
    var code: String,
    var label: String,
    var trailerId: String,
    var ratio: Double,
    var startsAt: Int,
    var attempts: Int,
    var score: Double,
    var status: ScheduledTransitTimeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTransitTimeEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTransitTimeEntryStatus): Boolean {
        when (status) {
            ScheduledTransitTimeEntryStatus.DRAFT -> {
                when (next) {
                    ScheduledTransitTimeEntryStatus.ACTIVE, ScheduledTransitTimeEntryStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledTransitTimeEntryStatus.PENDING -> {
                when (next) {
                    ScheduledTransitTimeEntryStatus.SUSPENDED, ScheduledTransitTimeEntryStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledTransitTimeEntryStatus.ACTIVE -> {
                when (next) {
                    ScheduledTransitTimeEntryStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledTransitTimeEntryStatus.SUSPENDED -> return next == ScheduledTransitTimeEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledTransitTimeEntryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled transit time entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round(attempts.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledTransitTimeEntry {
        return ScheduledTransitTimeEntry(
            code = code,
            label = label,
            trailerId = trailerId,
            ratio = ratio,
            startsAt = startsAt,
            attempts = attempts,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled transit time entry in status draft. */
fun newScheduledTransitTimeEntry(code: String, label: String): ScheduledTransitTimeEntry {
    return ScheduledTransitTimeEntry(
        code = code,
        label = label,
        trailerId = "",
        ratio = 0.0,
        startsAt = 0,
        attempts = 0,
        score = 0.0,
        status = ScheduledTransitTimeEntryStatus.DRAFT,
    )
}
