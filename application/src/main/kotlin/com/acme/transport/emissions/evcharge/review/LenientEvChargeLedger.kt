package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient ev charge ledger is in its life cycle. */
enum class LenientEvChargeLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient ev charge ledger and its current status. */
data class LenientEvChargeLedger(
    var code: String,
    var label: String,
    var floorValue: Double,
    var unit: String,
    var cost: Double,
    var checksum: String,
    var endsAt: Int,
    var status: LenientEvChargeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientEvChargeLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientEvChargeLedgerStatus): Boolean {
        when (status) {
            LenientEvChargeLedgerStatus.OPEN -> return next == LenientEvChargeLedgerStatus.REOPENED || next == LenientEvChargeLedgerStatus.ARCHIVED
            LenientEvChargeLedgerStatus.IN_REVIEW -> return next == LenientEvChargeLedgerStatus.RESOLVED || next == LenientEvChargeLedgerStatus.ARCHIVED
            LenientEvChargeLedgerStatus.RESOLVED -> return next == LenientEvChargeLedgerStatus.ARCHIVED
            LenientEvChargeLedgerStatus.REOPENED -> return next == LenientEvChargeLedgerStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientEvChargeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient ev charge ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((floorValue - cost) * 100) / 100

    fun snapshot(): LenientEvChargeLedger {
        return LenientEvChargeLedger(
            code = code,
            label = label,
            floorValue = floorValue,
            unit = unit,
            cost = cost,
            checksum = checksum,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient ev charge ledger in status open. */
fun newLenientEvChargeLedger(code: String, label: String): LenientEvChargeLedger {
    return LenientEvChargeLedger(
        code = code,
        label = label,
        floorValue = 0.0,
        unit = "",
        cost = 0.0,
        checksum = "",
        endsAt = 0,
        status = LenientEvChargeLedgerStatus.OPEN,
    )
}
