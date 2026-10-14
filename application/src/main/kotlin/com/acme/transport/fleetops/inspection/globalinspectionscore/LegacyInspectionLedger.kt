package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection ledger is in its life cycle. */
enum class LegacyInspectionLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy inspection ledger. Its code is unique within a tenant. */
data class LegacyInspectionLedger(
    var code: String,
    var title: String,
    var amount: Double,
    var ratio: Double,
    var externalRef: String,
    var status: LegacyInspectionLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionLedgerStatus): Boolean {
        when (status) {
            LegacyInspectionLedgerStatus.DRAFT -> return next == LegacyInspectionLedgerStatus.SUSPENDED || next == LegacyInspectionLedgerStatus.CLOSED
            LegacyInspectionLedgerStatus.PENDING -> return next == LegacyInspectionLedgerStatus.CLOSED
            LegacyInspectionLedgerStatus.ACTIVE -> return next == LegacyInspectionLedgerStatus.SUSPENDED
            LegacyInspectionLedgerStatus.SUSPENDED -> return next == LegacyInspectionLedgerStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyInspectionLedgerStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy inspection ledger"} $code"

    fun projectedExposure(): Double = round((amount - ratio) * 100) / 100

    fun snapshot(): LegacyInspectionLedger {
        return LegacyInspectionLedger(
            code = code,
            title = title,
            amount = amount,
            ratio = ratio,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy inspection ledger in status draft. */
fun newLegacyInspectionLedger(code: String, title: String): LegacyInspectionLedger {
    return LegacyInspectionLedger(
        code = code,
        title = title,
        amount = 0.0,
        ratio = 0.0,
        externalRef = "",
        status = LegacyInspectionLedgerStatus.DRAFT,
    )
}
