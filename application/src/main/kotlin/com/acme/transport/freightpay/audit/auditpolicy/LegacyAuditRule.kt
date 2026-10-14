package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy audit rule is in its life cycle. */
enum class LegacyAuditRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy audit rule. Its code is unique within a tenant. */
data class LegacyAuditRule(
    var code: String,
    var name: String,
    var ceiling: Double,
    var loadKg: Double,
    var tags: MutableList<String>,
    var notes: String,
    var phone: String,
    var status: LegacyAuditRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAuditRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAuditRuleStatus): Boolean {
        when (status) {
            LegacyAuditRuleStatus.PROPOSED -> {
                when (next) {
                    LegacyAuditRuleStatus.APPROVED, LegacyAuditRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyAuditRuleStatus.APPROVED -> return next == LegacyAuditRuleStatus.REJECTED || next == LegacyAuditRuleStatus.WITHDRAWN
            LegacyAuditRuleStatus.REJECTED -> return next == LegacyAuditRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyAuditRuleStatus) {
        if (!canMoveTo(next)) {
            error("legacy audit rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy audit rule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(ceiling * loadKg * 100) / 100

    fun snapshot(): LegacyAuditRule {
        return LegacyAuditRule(
            code = code,
            name = name,
            ceiling = ceiling,
            loadKg = loadKg,
            tags = tags,
            notes = notes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy audit rule in status proposed. */
fun newLegacyAuditRule(code: String, name: String): LegacyAuditRule {
    return LegacyAuditRule(
        code = code,
        name = name,
        ceiling = 0.0,
        loadKg = 0.0,
        tags = mutableListOf<String>(),
        notes = "",
        phone = "",
        status = LegacyAuditRuleStatus.PROPOSED,
    )
}
