package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge ledger is in its life cycle. */
enum class EvChargeLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a ev charge ledger and its current status. */
data class EvChargeLedger(
    var code: String,
    var title: String,
    var depot: String,
    var visible: Boolean,
    var threshold: Double,
    var stopCount: Int,
    var graceDays: Int,
    var externalRef: String,
    var expiresAt: Int,
    var status: EvChargeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeLedgerStatus): Boolean {
        when (status) {
            EvChargeLedgerStatus.CREATED -> return next == EvChargeLedgerStatus.VALIDATED
            EvChargeLedgerStatus.VALIDATED -> {
                when (next) {
                    EvChargeLedgerStatus.PUBLISHED, EvChargeLedgerStatus.RETIRED -> return true
                }
                return false
            }
            EvChargeLedgerStatus.PUBLISHED -> return next == EvChargeLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: EvChargeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("ev charge ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"ev charge ledger"} $code"

    fun netValue(): Double = round((stopCount.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): EvChargeLedger {
        return EvChargeLedger(
            code = code,
            title = title,
            depot = depot,
            visible = visible,
            threshold = threshold,
            stopCount = stopCount,
            graceDays = graceDays,
            externalRef = externalRef,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a ev charge ledger in status created. */
fun newEvChargeLedger(code: String, title: String): EvChargeLedger {
    return EvChargeLedger(
        code = code,
        title = title,
        depot = "",
        visible = false,
        threshold = 0.0,
        stopCount = 0,
        graceDays = 0,
        externalRef = "",
        expiresAt = 0,
        status = EvChargeLedgerStatus.CREATED,
    )
}
