package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive hold ledger is in its life cycle. */
enum class AdaptiveHoldLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive hold ledger as the hold team keeps it. */
data class AdaptiveHoldLedger(
    var code: String,
    var label: String,
    var floorValue: Double,
    var margin: Double,
    var reviewer: String,
    var status: AdaptiveHoldLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveHoldLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveHoldLedgerStatus): Boolean {
        when (status) {
            AdaptiveHoldLedgerStatus.CREATED -> {
                when (next) {
                    AdaptiveHoldLedgerStatus.PUBLISHED -> return true
                }
                return false
            }
            AdaptiveHoldLedgerStatus.VALIDATED -> return next == AdaptiveHoldLedgerStatus.PUBLISHED || next == AdaptiveHoldLedgerStatus.RETIRED
            AdaptiveHoldLedgerStatus.PUBLISHED -> return next == AdaptiveHoldLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveHoldLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive hold ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((margin - floorValue) * 100) / 100

    fun snapshot(): AdaptiveHoldLedger {
        return AdaptiveHoldLedger(
            code = code,
            label = label,
            floorValue = floorValue,
            margin = margin,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive hold ledger in status created. */
fun newAdaptiveHoldLedger(code: String, label: String): AdaptiveHoldLedger {
    return AdaptiveHoldLedger(
        code = code,
        label = label,
        floorValue = 0.0,
        margin = 0.0,
        reviewer = "",
        status = AdaptiveHoldLedgerStatus.CREATED,
    )
}
