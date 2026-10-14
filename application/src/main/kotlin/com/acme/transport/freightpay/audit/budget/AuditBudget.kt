package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit budget records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit budget is in its life cycle. */
enum class AuditBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A audit budget. Its code is unique within a tenant. */
data class AuditBudget(
    var code: String,
    var title: String,
    var threshold: Double,
    var stopCount: Int,
    var ratio: Double,
    var status: AuditBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditBudgetStatus): Boolean {
        when (status) {
            AuditBudgetStatus.OPEN -> return next == AuditBudgetStatus.REOPENED
            AuditBudgetStatus.IN_REVIEW -> {
                when (next) {
                    AuditBudgetStatus.REOPENED -> return true
                }
                return false
            }
            AuditBudgetStatus.RESOLVED -> {
                when (next) {
                    AuditBudgetStatus.REOPENED -> return true
                }
                return false
            }
            AuditBudgetStatus.REOPENED -> return next == AuditBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AuditBudgetStatus) {
        if (!canMoveTo(next)) {
            error("audit budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"audit budget"} $code"

    fun netValue(): Double = round((ratio - threshold) * 100) / 100

    fun snapshot(): AuditBudget {
        return AuditBudget(
            code = code,
            title = title,
            threshold = threshold,
            stopCount = stopCount,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit budget in status open. */
fun newAuditBudget(code: String, title: String): AuditBudget {
    return AuditBudget(
        code = code,
        title = title,
        threshold = 0.0,
        stopCount = 0,
        ratio = 0.0,
        status = AuditBudgetStatus.OPEN,
    )
}
