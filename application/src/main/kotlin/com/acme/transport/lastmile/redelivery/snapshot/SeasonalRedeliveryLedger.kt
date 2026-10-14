package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery ledger is in its life cycle. */
enum class SeasonalRedeliveryLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One seasonal redelivery ledger, with what the planners need to act on it. */
data class SeasonalRedeliveryLedger(
    var code: String,
    var label: String,
    var pallets: Int,
    var revision: Int,
    var description: String,
    var status: SeasonalRedeliveryLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryLedgerStatus): Boolean {
        when (status) {
            SeasonalRedeliveryLedgerStatus.NEW -> {
                when (next) {
                    SeasonalRedeliveryLedgerStatus.RUNNING, SeasonalRedeliveryLedgerStatus.DONE -> return true
                }
                return false
            }
            SeasonalRedeliveryLedgerStatus.QUEUED -> return next == SeasonalRedeliveryLedgerStatus.RUNNING
            SeasonalRedeliveryLedgerStatus.RUNNING -> {
                when (next) {
                    SeasonalRedeliveryLedgerStatus.DONE -> return true
                }
                return false
            }
            SeasonalRedeliveryLedgerStatus.DONE -> {
                when (next) {
                    SeasonalRedeliveryLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"seasonal redelivery ledger"} $code"

    fun projectedTotal(): Double = round((pallets.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRedeliveryLedger {
        return SeasonalRedeliveryLedger(
            code = code,
            label = label,
            pallets = pallets,
            revision = revision,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal redelivery ledger in status new. */
fun newSeasonalRedeliveryLedger(code: String, label: String): SeasonalRedeliveryLedger {
    return SeasonalRedeliveryLedger(
        code = code,
        label = label,
        pallets = 0,
        revision = 0,
        description = "",
        status = SeasonalRedeliveryLedgerStatus.NEW,
    )
}
