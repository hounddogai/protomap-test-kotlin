package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive drop ledger is in its life cycle. */
enum class AdaptiveDropLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive drop ledger and its current status. */
data class AdaptiveDropLedger(
    var code: String,
    var title: String,
    var checksum: String,
    var odometer: Int,
    var label: String,
    var endsAt: Int,
    var status: AdaptiveDropLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDropLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDropLedgerStatus): Boolean {
        when (status) {
            AdaptiveDropLedgerStatus.IDLE -> return next == AdaptiveDropLedgerStatus.READY || next == AdaptiveDropLedgerStatus.STOPPED
            AdaptiveDropLedgerStatus.WARMING -> return next == AdaptiveDropLedgerStatus.STOPPED
            AdaptiveDropLedgerStatus.READY -> {
                when (next) {
                    AdaptiveDropLedgerStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveDropLedgerStatus.DRAINING -> return next == AdaptiveDropLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveDropLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive drop ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive drop ledger"} $code"

    fun effectiveValue(): Double = round((endsAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDropLedger {
        return AdaptiveDropLedger(
            code = code,
            title = title,
            checksum = checksum,
            odometer = odometer,
            label = label,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive drop ledger in status idle. */
fun newAdaptiveDropLedger(code: String, title: String): AdaptiveDropLedger {
    return AdaptiveDropLedger(
        code = code,
        title = title,
        checksum = "",
        odometer = 0,
        label = "",
        endsAt = 0,
        status = AdaptiveDropLedgerStatus.IDLE,
    )
}
