package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.normalize

/** Where a batched dispute rule is in its life cycle. */
enum class BatchedDisputeRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched dispute rule. Its code is unique within a tenant. */
data class BatchedDisputeRule(
    var code: String,
    var name: String,
    var phone: String,
    var summary: String,
    var source: String,
    var durationMinutes: Int,
    var locale: String,
    var status: BatchedDisputeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDisputeRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDisputeRuleStatus): Boolean {
        when (status) {
            BatchedDisputeRuleStatus.OPEN -> return next == BatchedDisputeRuleStatus.RESOLVED || next == BatchedDisputeRuleStatus.ARCHIVED
            BatchedDisputeRuleStatus.IN_REVIEW -> return next == BatchedDisputeRuleStatus.ARCHIVED
            BatchedDisputeRuleStatus.RESOLVED -> return next == BatchedDisputeRuleStatus.REOPENED || next == BatchedDisputeRuleStatus.ARCHIVED
            BatchedDisputeRuleStatus.REOPENED -> {
                when (next) {
                    BatchedDisputeRuleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDisputeRuleStatus) {
        if (!canMoveTo(next)) {
            error("batched dispute rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): BatchedDisputeRule {
        return BatchedDisputeRule(
            code = code,
            name = name,
            phone = phone,
            summary = summary,
            source = source,
            durationMinutes = durationMinutes,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched dispute rule in status open. */
fun newBatchedDisputeRule(code: String, name: String): BatchedDisputeRule {
    return BatchedDisputeRule(
        code = code,
        name = name,
        phone = "",
        summary = "",
        source = "",
        durationMinutes = 0,
        locale = "",
        status = BatchedDisputeRuleStatus.OPEN,
    )
}
