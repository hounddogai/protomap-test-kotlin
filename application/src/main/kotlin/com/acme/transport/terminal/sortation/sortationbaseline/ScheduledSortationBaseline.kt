package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled sortation baseline records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a scheduled sortation baseline is in its life cycle. */
enum class ScheduledSortationBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled sortation baseline as the sortation team keeps it. */
data class ScheduledSortationBaseline(
    var code: String,
    var name: String,
    var carrier: String,
    var priority: Int,
    var startsAt: Int,
    var status: ScheduledSortationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSortationBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSortationBaselineStatus): Boolean {
        when (status) {
            ScheduledSortationBaselineStatus.DRAFT -> {
                when (next) {
                    ScheduledSortationBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledSortationBaselineStatus.PENDING -> {
                when (next) {
                    ScheduledSortationBaselineStatus.SUSPENDED, ScheduledSortationBaselineStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledSortationBaselineStatus.ACTIVE -> {
                when (next) {
                    ScheduledSortationBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledSortationBaselineStatus.SUSPENDED -> {
                when (next) {
                    ScheduledSortationBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSortationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("scheduled sortation baseline $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((startsAt.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): ScheduledSortationBaseline {
        return ScheduledSortationBaseline(
            code = code,
            name = name,
            carrier = carrier,
            priority = priority,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled sortation baseline in status draft. */
fun newScheduledSortationBaseline(code: String, name: String): ScheduledSortationBaseline {
    return ScheduledSortationBaseline(
        code = code,
        name = name,
        carrier = "",
        priority = 0,
        startsAt = 0,
        status = ScheduledSortationBaselineStatus.DRAFT,
    )
}
