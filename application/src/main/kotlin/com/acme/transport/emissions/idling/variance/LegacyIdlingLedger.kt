package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy idling ledger is in its life cycle. */
enum class LegacyIdlingLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy idling ledger. Its code is unique within a tenant. */
data class LegacyIdlingLedger(
    var code: String,
    var label: String,
    var title: String,
    var cost: Double,
    var summary: String,
    var expiresAt: Int,
    var retries: Int,
    var shiftCode: String,
    var score: Double,
    var status: LegacyIdlingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIdlingLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIdlingLedgerStatus): Boolean {
        when (status) {
            LegacyIdlingLedgerStatus.PROPOSED -> return next == LegacyIdlingLedgerStatus.REJECTED
            LegacyIdlingLedgerStatus.APPROVED -> return next == LegacyIdlingLedgerStatus.REJECTED
            LegacyIdlingLedgerStatus.REJECTED -> {
                when (next) {
                    LegacyIdlingLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyIdlingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("legacy idling ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy idling ledger"} $code"

    fun effectiveTotal(): Double = round(expiresAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): LegacyIdlingLedger {
        return LegacyIdlingLedger(
            code = code,
            label = label,
            title = title,
            cost = cost,
            summary = summary,
            expiresAt = expiresAt,
            retries = retries,
            shiftCode = shiftCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy idling ledger in status proposed. */
fun newLegacyIdlingLedger(code: String, label: String): LegacyIdlingLedger {
    return LegacyIdlingLedger(
        code = code,
        label = label,
        title = "",
        cost = 0.0,
        summary = "",
        expiresAt = 0,
        retries = 0,
        shiftCode = "",
        score = 0.0,
        status = LegacyIdlingLedgerStatus.PROPOSED,
    )
}
