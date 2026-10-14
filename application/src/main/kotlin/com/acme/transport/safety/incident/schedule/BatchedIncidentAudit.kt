package com.acme.transport.safety.incident.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched incident audit is in its life cycle. */
enum class BatchedIncidentAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched incident audit and its current status. */
data class BatchedIncidentAudit(
    var code: String,
    var title: String,
    var pallets: Int,
    var revision: Int,
    var budget: Double,
    var source: String,
    var amount: Double,
    var status: BatchedIncidentAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIncidentAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIncidentAuditStatus): Boolean {
        when (status) {
            BatchedIncidentAuditStatus.NEW -> return next == BatchedIncidentAuditStatus.QUEUED
            BatchedIncidentAuditStatus.QUEUED -> return next == BatchedIncidentAuditStatus.FAILED
            BatchedIncidentAuditStatus.RUNNING -> return next == BatchedIncidentAuditStatus.DONE
            BatchedIncidentAuditStatus.DONE -> return next == BatchedIncidentAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedIncidentAuditStatus) {
        if (!canMoveTo(next)) {
            error("batched incident audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"batched incident audit"} $code"

    fun weightedValue(): Double = round(amount * pallets.toDouble() * 100) / 100

    fun snapshot(): BatchedIncidentAudit {
        return BatchedIncidentAudit(
            code = code,
            title = title,
            pallets = pallets,
            revision = revision,
            budget = budget,
            source = source,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched incident audit in status new. */
fun newBatchedIncidentAudit(code: String, title: String): BatchedIncidentAudit {
    return BatchedIncidentAudit(
        code = code,
        title = title,
        pallets = 0,
        revision = 0,
        budget = 0.0,
        source = "",
        amount = 0.0,
        status = BatchedIncidentAuditStatus.NEW,
    )
}
