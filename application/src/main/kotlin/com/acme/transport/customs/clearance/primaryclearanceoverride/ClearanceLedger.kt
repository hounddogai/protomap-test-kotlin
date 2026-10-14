package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance ledger is in its life cycle. */
enum class ClearanceLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A clearance ledger. Its code is unique within a tenant. */
data class ClearanceLedger(
    var code: String,
    var name: String,
    var loadKg: Double,
    var segment: String,
    var dock: String,
    var stopCount: Int,
    var discount: Double,
    var archived: Boolean,
    var status: ClearanceLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceLedgerStatus): Boolean {
        when (status) {
            ClearanceLedgerStatus.DRAFT -> {
                when (next) {
                    ClearanceLedgerStatus.SUSPENDED -> return true
                }
                return false
            }
            ClearanceLedgerStatus.PENDING -> return next == ClearanceLedgerStatus.ACTIVE || next == ClearanceLedgerStatus.SUSPENDED
            ClearanceLedgerStatus.ACTIVE -> {
                when (next) {
                    ClearanceLedgerStatus.SUSPENDED -> return true
                }
                return false
            }
            ClearanceLedgerStatus.SUSPENDED -> return next == ClearanceLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ClearanceLedgerStatus) {
        if (!canMoveTo(next)) {
            error("clearance ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((stopCount.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ClearanceLedger {
        return ClearanceLedger(
            code = code,
            name = name,
            loadKg = loadKg,
            segment = segment,
            dock = dock,
            stopCount = stopCount,
            discount = discount,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a clearance ledger in status draft. */
fun newClearanceLedger(code: String, name: String): ClearanceLedger {
    return ClearanceLedger(
        code = code,
        name = name,
        loadKg = 0.0,
        segment = "",
        dock = "",
        stopCount = 0,
        discount = 0.0,
        archived = false,
        status = ClearanceLedgerStatus.DRAFT,
    )
}
