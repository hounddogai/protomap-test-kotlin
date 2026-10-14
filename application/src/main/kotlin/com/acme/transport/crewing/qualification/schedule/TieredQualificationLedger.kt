package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered qualification ledger is in its life cycle. */
enum class TieredQualificationLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered qualification ledger. Its code is unique within a tenant. */
data class TieredQualificationLedger(
    var code: String,
    var title: String,
    var retries: Int,
    var limit: Int,
    var budget: Double,
    var ceiling: Double,
    var status: TieredQualificationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredQualificationLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredQualificationLedgerStatus): Boolean {
        when (status) {
            TieredQualificationLedgerStatus.PROPOSED -> return next == TieredQualificationLedgerStatus.APPROVED || next == TieredQualificationLedgerStatus.REJECTED
            TieredQualificationLedgerStatus.APPROVED -> return next == TieredQualificationLedgerStatus.WITHDRAWN
            TieredQualificationLedgerStatus.REJECTED -> return next == TieredQualificationLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredQualificationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("tiered qualification ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((ceiling + budget) * 100) / 100

    fun snapshot(): TieredQualificationLedger {
        return TieredQualificationLedger(
            code = code,
            title = title,
            retries = retries,
            limit = limit,
            budget = budget,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered qualification ledger in status proposed. */
fun newTieredQualificationLedger(code: String, title: String): TieredQualificationLedger {
    return TieredQualificationLedger(
        code = code,
        title = title,
        retries = 0,
        limit = 0,
        budget = 0.0,
        ceiling = 0.0,
        status = TieredQualificationLedgerStatus.PROPOSED,
    )
}
