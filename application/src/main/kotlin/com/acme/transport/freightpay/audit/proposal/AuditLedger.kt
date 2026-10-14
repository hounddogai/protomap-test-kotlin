package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit ledger is in its life cycle. */
enum class AuditLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit ledger as the audit team keeps it. */
data class AuditLedger(
    var code: String,
    var name: String,
    var maximum: Double,
    var trailerId: String,
    var revision: Int,
    var category: String,
    var active: Boolean,
    var ratio: Double,
    var status: AuditLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditLedgerStatus): Boolean {
        when (status) {
            AuditLedgerStatus.PROPOSED -> return next == AuditLedgerStatus.APPROVED || next == AuditLedgerStatus.REJECTED
            AuditLedgerStatus.APPROVED -> {
                when (next) {
                    AuditLedgerStatus.REJECTED -> return true
                }
                return false
            }
            AuditLedgerStatus.REJECTED -> {
                when (next) {
                    AuditLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditLedgerStatus) {
        if (!canMoveTo(next)) {
            error("audit ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"audit ledger"} $code"

    fun weightedVolume(): Double = round((revision.toDouble() - ratio) * 100) / 100

    fun snapshot(): AuditLedger {
        return AuditLedger(
            code = code,
            name = name,
            maximum = maximum,
            trailerId = trailerId,
            revision = revision,
            category = category,
            active = active,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a audit ledger in status proposed. */
fun newAuditLedger(code: String, name: String): AuditLedger {
    return AuditLedger(
        code = code,
        name = name,
        maximum = 0.0,
        trailerId = "",
        revision = 0,
        category = "",
        active = false,
        ratio = 0.0,
        status = AuditLedgerStatus.PROPOSED,
    )
}
