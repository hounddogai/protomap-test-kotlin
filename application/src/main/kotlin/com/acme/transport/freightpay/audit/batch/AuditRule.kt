package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit rule is in its life cycle. */
enum class AuditRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a audit rule and its current status. */
data class AuditRule(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var trailerId: String,
    var batchSize: Int,
    var status: AuditRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditRuleStatus): Boolean {
        when (status) {
            AuditRuleStatus.PROPOSED -> {
                when (next) {
                    AuditRuleStatus.APPROVED, AuditRuleStatus.REJECTED -> return true
                }
                return false
            }
            AuditRuleStatus.APPROVED -> {
                when (next) {
                    AuditRuleStatus.REJECTED, AuditRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            AuditRuleStatus.REJECTED -> {
                when (next) {
                    AuditRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditRuleStatus) {
        if (!canMoveTo(next)) {
            error("audit rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round((updatedAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): AuditRule {
        return AuditRule(
            code = code,
            title = title,
            updatedAt = updatedAt,
            trailerId = trailerId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit rule in status proposed. */
fun newAuditRule(code: String, title: String): AuditRule {
    return AuditRule(
        code = code,
        title = title,
        updatedAt = 0,
        trailerId = "",
        batchSize = 0,
        status = AuditRuleStatus.PROPOSED,
    )
}
