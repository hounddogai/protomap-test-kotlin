package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling audit ledger is in its life cycle. */
enum class RollingAuditLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling audit ledger and its current status. */
data class RollingAuditLedger(
    var code: String,
    var name: String,
    var driverId: String,
    var tags: MutableList<String>,
    var source: String,
    var margin: Double,
    var timezone: String,
    var shiftCode: String,
    var batchSize: Int,
    var amount: Double,
    var limit: Int,
    var status: RollingAuditLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAuditLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAuditLedgerStatus): Boolean {
        when (status) {
            RollingAuditLedgerStatus.PROPOSED -> {
                when (next) {
                    RollingAuditLedgerStatus.APPROVED, RollingAuditLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingAuditLedgerStatus.APPROVED -> return next == RollingAuditLedgerStatus.REJECTED
            RollingAuditLedgerStatus.REJECTED -> {
                when (next) {
                    RollingAuditLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAuditLedgerStatus) {
        if (!canMoveTo(next)) {
            error("rolling audit ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((batchSize.toDouble() + margin) * 100) / 100

    fun snapshot(): RollingAuditLedger {
        return RollingAuditLedger(
            code = code,
            name = name,
            driverId = driverId,
            tags = tags,
            source = source,
            margin = margin,
            timezone = timezone,
            shiftCode = shiftCode,
            batchSize = batchSize,
            amount = amount,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling audit ledger in status proposed. */
fun newRollingAuditLedger(code: String, name: String): RollingAuditLedger {
    return RollingAuditLedger(
        code = code,
        name = name,
        driverId = "",
        tags = mutableListOf<String>(),
        source = "",
        margin = 0.0,
        timezone = "",
        shiftCode = "",
        batchSize = 0,
        amount = 0.0,
        limit = 0,
        status = RollingAuditLedgerStatus.PROPOSED,
    )
}
