package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal drop entry records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a seasonal drop entry is in its life cycle. */
enum class SeasonalDropEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One seasonal drop entry, with what the planners need to act on it. */
data class SeasonalDropEntry(
    var code: String,
    var name: String,
    var minimum: Double,
    var revision: Int,
    var cost: Double,
    var reviewer: String,
    var amount: Double,
    var segment: String,
    var status: SeasonalDropEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDropEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDropEntryStatus): Boolean {
        when (status) {
            SeasonalDropEntryStatus.NEW -> return next == SeasonalDropEntryStatus.RUNNING || next == SeasonalDropEntryStatus.FAILED
            SeasonalDropEntryStatus.QUEUED -> {
                when (next) {
                    SeasonalDropEntryStatus.DONE -> return true
                }
                return false
            }
            SeasonalDropEntryStatus.RUNNING -> return next == SeasonalDropEntryStatus.DONE
            SeasonalDropEntryStatus.DONE -> return next == SeasonalDropEntryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalDropEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal drop entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"seasonal drop entry"} $code"

    fun effectiveTotal(): Double = round((minimum - amount) * 100) / 100

    fun snapshot(): SeasonalDropEntry {
        return SeasonalDropEntry(
            code = code,
            name = name,
            minimum = minimum,
            revision = revision,
            cost = cost,
            reviewer = reviewer,
            amount = amount,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal drop entry in status new. */
fun newSeasonalDropEntry(code: String, name: String): SeasonalDropEntry {
    return SeasonalDropEntry(
        code = code,
        name = name,
        minimum = 0.0,
        revision = 0,
        cost = 0.0,
        reviewer = "",
        amount = 0.0,
        segment = "",
        status = SeasonalDropEntryStatus.NEW,
    )
}
