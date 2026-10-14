package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold ledger is in its life cycle. */
enum class HoldLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hold ledger. Its code is unique within a tenant. */
data class HoldLedger(
    var code: String,
    var label: String,
    var segment: String,
    var visible: Boolean,
    var cost: Double,
    var summary: String,
    var routeCode: String,
    var loadKg: Double,
    var dock: String,
    var status: HoldLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldLedgerStatus): Boolean {
        when (status) {
            HoldLedgerStatus.NEW -> {
                when (next) {
                    HoldLedgerStatus.DONE -> return true
                }
                return false
            }
            HoldLedgerStatus.QUEUED -> {
                when (next) {
                    HoldLedgerStatus.DONE, HoldLedgerStatus.FAILED -> return true
                }
                return false
            }
            HoldLedgerStatus.RUNNING -> return next == HoldLedgerStatus.DONE || next == HoldLedgerStatus.FAILED
            HoldLedgerStatus.DONE -> return next == HoldLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: HoldLedgerStatus) {
        if (!canMoveTo(next)) {
            error("hold ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round((cost + loadKg) * 100) / 100

    fun snapshot(): HoldLedger {
        return HoldLedger(
            code = code,
            label = label,
            segment = segment,
            visible = visible,
            cost = cost,
            summary = summary,
            routeCode = routeCode,
            loadKg = loadKg,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a hold ledger in status new. */
fun newHoldLedger(code: String, label: String): HoldLedger {
    return HoldLedger(
        code = code,
        label = label,
        segment = "",
        visible = false,
        cost = 0.0,
        summary = "",
        routeCode = "",
        loadKg = 0.0,
        dock = "",
        status = HoldLedgerStatus.NEW,
    )
}
