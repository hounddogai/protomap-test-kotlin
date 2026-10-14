package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal attempt ledger is in its life cycle. */
enum class SeasonalAttemptLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal attempt ledger. Its code is unique within a tenant. */
data class SeasonalAttemptLedger(
    var code: String,
    var title: String,
    var createdAt: Int,
    var ceiling: Double,
    var dock: String,
    var attempts: Int,
    var status: SeasonalAttemptLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAttemptLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAttemptLedgerStatus): Boolean {
        when (status) {
            SeasonalAttemptLedgerStatus.IDLE -> {
                when (next) {
                    SeasonalAttemptLedgerStatus.READY, SeasonalAttemptLedgerStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalAttemptLedgerStatus.WARMING -> {
                when (next) {
                    SeasonalAttemptLedgerStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalAttemptLedgerStatus.READY -> {
                when (next) {
                    SeasonalAttemptLedgerStatus.DRAINING, SeasonalAttemptLedgerStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalAttemptLedgerStatus.DRAINING -> {
                when (next) {
                    SeasonalAttemptLedgerStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAttemptLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal attempt ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round((attempts.toDouble() - ceiling) * 100) / 100

    fun snapshot(): SeasonalAttemptLedger {
        return SeasonalAttemptLedger(
            code = code,
            title = title,
            createdAt = createdAt,
            ceiling = ceiling,
            dock = dock,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal attempt ledger in status idle. */
fun newSeasonalAttemptLedger(code: String, title: String): SeasonalAttemptLedger {
    return SeasonalAttemptLedger(
        code = code,
        title = title,
        createdAt = 0,
        ceiling = 0.0,
        dock = "",
        attempts = 0,
        status = SeasonalAttemptLedgerStatus.IDLE,
    )
}
