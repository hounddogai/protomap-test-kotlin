package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy scorecard ledger records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a legacy scorecard ledger is in its life cycle. */
enum class LegacyScorecardLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy scorecard ledger as the scorecard team keeps it. */
data class LegacyScorecardLedger(
    var code: String,
    var name: String,
    var budget: Double,
    var dock: String,
    var revision: Int,
    var status: LegacyScorecardLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyScorecardLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyScorecardLedgerStatus): Boolean {
        when (status) {
            LegacyScorecardLedgerStatus.IDLE -> return next == LegacyScorecardLedgerStatus.STOPPED
            LegacyScorecardLedgerStatus.WARMING -> {
                when (next) {
                    LegacyScorecardLedgerStatus.READY -> return true
                }
                return false
            }
            LegacyScorecardLedgerStatus.READY -> {
                when (next) {
                    LegacyScorecardLedgerStatus.STOPPED -> return true
                }
                return false
            }
            LegacyScorecardLedgerStatus.DRAINING -> return next == LegacyScorecardLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyScorecardLedgerStatus) {
        if (!canMoveTo(next)) {
            error("legacy scorecard ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((budget + revision.toDouble()) * 100) / 100

    fun snapshot(): LegacyScorecardLedger {
        return LegacyScorecardLedger(
            code = code,
            name = name,
            budget = budget,
            dock = dock,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy scorecard ledger in status idle. */
fun newLegacyScorecardLedger(code: String, name: String): LegacyScorecardLedger {
    return LegacyScorecardLedger(
        code = code,
        name = name,
        budget = 0.0,
        dock = "",
        revision = 0,
        status = LegacyScorecardLedgerStatus.IDLE,
    )
}
