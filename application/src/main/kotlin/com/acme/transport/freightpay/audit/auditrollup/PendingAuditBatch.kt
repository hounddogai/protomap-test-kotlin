package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.kit.text.normalize

/** Where a pending audit batch is in its life cycle. */
enum class PendingAuditBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending audit batch as the audit team keeps it. */
data class PendingAuditBatch(
    var code: String,
    var label: String,
    var dock: String,
    var rate: Double,
    var currency: String,
    var status: PendingAuditBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAuditBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAuditBatchStatus): Boolean {
        when (status) {
            PendingAuditBatchStatus.PLANNED -> {
                when (next) {
                    PendingAuditBatchStatus.SCHEDULED, PendingAuditBatchStatus.COMPLETED -> return true
                }
                return false
            }
            PendingAuditBatchStatus.SCHEDULED -> return next == PendingAuditBatchStatus.CANCELLED
            PendingAuditBatchStatus.IN_PROGRESS -> return next == PendingAuditBatchStatus.COMPLETED || next == PendingAuditBatchStatus.CANCELLED
            PendingAuditBatchStatus.COMPLETED -> {
                when (next) {
                    PendingAuditBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAuditBatchStatus) {
        if (!canMoveTo(next)) {
            error("pending audit batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = rate

    fun snapshot(): PendingAuditBatch {
        return PendingAuditBatch(
            code = code,
            label = label,
            dock = dock,
            rate = rate,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending audit batch in status planned. */
fun newPendingAuditBatch(code: String, label: String): PendingAuditBatch {
    return PendingAuditBatch(
        code = code,
        label = label,
        dock = "",
        rate = 0.0,
        currency = "",
        status = PendingAuditBatchStatus.PLANNED,
    )
}
