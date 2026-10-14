package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional audit rollup records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a regional audit rollup is in its life cycle. */
enum class RegionalAuditRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional audit rollup as the audit team keeps it. */
data class RegionalAuditRollup(
    var code: String,
    var name: String,
    var budget: Double,
    var quantity: Int,
    var rate: Double,
    var expiresAt: Int,
    var status: RegionalAuditRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAuditRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAuditRollupStatus): Boolean {
        when (status) {
            RegionalAuditRollupStatus.CREATED -> return next == RegionalAuditRollupStatus.RETIRED
            RegionalAuditRollupStatus.VALIDATED -> return next == RegionalAuditRollupStatus.PUBLISHED
            RegionalAuditRollupStatus.PUBLISHED -> return next == RegionalAuditRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalAuditRollupStatus) {
        if (!canMoveTo(next)) {
            error("regional audit rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round((rate + quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalAuditRollup {
        return RegionalAuditRollup(
            code = code,
            name = name,
            budget = budget,
            quantity = quantity,
            rate = rate,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional audit rollup in status created. */
fun newRegionalAuditRollup(code: String, name: String): RegionalAuditRollup {
    return RegionalAuditRollup(
        code = code,
        name = name,
        budget = 0.0,
        quantity = 0,
        rate = 0.0,
        expiresAt = 0,
        status = RegionalAuditRollupStatus.CREATED,
    )
}
