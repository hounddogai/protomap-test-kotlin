package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal rest break ledger is in its life cycle. */
enum class SeasonalRestBreakLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal rest break ledger as the rest break team keeps it. */
data class SeasonalRestBreakLedger(
    var code: String,
    var title: String,
    var description: String,
    var maximum: Double,
    var retries: Int,
    var label: String,
    var minimum: Double,
    var status: SeasonalRestBreakLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRestBreakLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRestBreakLedgerStatus): Boolean {
        when (status) {
            SeasonalRestBreakLedgerStatus.NEW -> {
                when (next) {
                    SeasonalRestBreakLedgerStatus.QUEUED, SeasonalRestBreakLedgerStatus.FAILED -> return true
                }
                return false
            }
            SeasonalRestBreakLedgerStatus.QUEUED -> {
                when (next) {
                    SeasonalRestBreakLedgerStatus.RUNNING, SeasonalRestBreakLedgerStatus.FAILED -> return true
                }
                return false
            }
            SeasonalRestBreakLedgerStatus.RUNNING -> {
                when (next) {
                    SeasonalRestBreakLedgerStatus.DONE -> return true
                }
                return false
            }
            SeasonalRestBreakLedgerStatus.DONE -> return next == SeasonalRestBreakLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalRestBreakLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal rest break ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun projectedLoad(): Double = round((minimum - maximum) * 100) / 100

    fun snapshot(): SeasonalRestBreakLedger {
        return SeasonalRestBreakLedger(
            code = code,
            title = title,
            description = description,
            maximum = maximum,
            retries = retries,
            label = label,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal rest break ledger in status new. */
fun newSeasonalRestBreakLedger(code: String, title: String): SeasonalRestBreakLedger {
    return SeasonalRestBreakLedger(
        code = code,
        title = title,
        description = "",
        maximum = 0.0,
        retries = 0,
        label = "",
        minimum = 0.0,
        status = SeasonalRestBreakLedgerStatus.NEW,
    )
}
