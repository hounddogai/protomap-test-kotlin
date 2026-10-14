package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap ledger is in its life cycle. */
enum class SwapLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A swap ledger. Its code is unique within a tenant. */
data class SwapLedger(
    var code: String,
    var title: String,
    var locale: String,
    var priority: Int,
    var amount: Double,
    var visible: Boolean,
    var floorValue: Double,
    var externalRef: String,
    var vehicleId: String,
    var status: SwapLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapLedgerStatus): Boolean {
        when (status) {
            SwapLedgerStatus.REQUESTED -> {
                when (next) {
                    SwapLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            SwapLedgerStatus.GRANTED -> return next == SwapLedgerStatus.EXPIRED
            SwapLedgerStatus.REVOKED -> return next == SwapLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SwapLedgerStatus) {
        if (!canMoveTo(next)) {
            error("swap ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((priority.toDouble() - floorValue) * 100) / 100

    fun snapshot(): SwapLedger {
        return SwapLedger(
            code = code,
            title = title,
            locale = locale,
            priority = priority,
            amount = amount,
            visible = visible,
            floorValue = floorValue,
            externalRef = externalRef,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a swap ledger in status requested. */
fun newSwapLedger(code: String, title: String): SwapLedger {
    return SwapLedger(
        code = code,
        title = title,
        locale = "",
        priority = 0,
        amount = 0.0,
        visible = false,
        floorValue = 0.0,
        externalRef = "",
        vehicleId = "",
        status = SwapLedgerStatus.REQUESTED,
    )
}
