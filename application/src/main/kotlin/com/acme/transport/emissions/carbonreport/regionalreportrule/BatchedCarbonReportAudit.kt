package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched carbon report audit is in its life cycle. */
enum class BatchedCarbonReportAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched carbon report audit, with what the planners need to act on it. */
data class BatchedCarbonReportAudit(
    var code: String,
    var name: String,
    var externalRef: String,
    var label: String,
    var cost: Double,
    var limit: Int,
    var status: BatchedCarbonReportAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCarbonReportAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCarbonReportAuditStatus): Boolean {
        when (status) {
            BatchedCarbonReportAuditStatus.PLANNED -> {
                when (next) {
                    BatchedCarbonReportAuditStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedCarbonReportAuditStatus.SCHEDULED -> return next == BatchedCarbonReportAuditStatus.IN_PROGRESS
            BatchedCarbonReportAuditStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedCarbonReportAuditStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedCarbonReportAuditStatus.COMPLETED -> return next == BatchedCarbonReportAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedCarbonReportAuditStatus) {
        if (!canMoveTo(next)) {
            error("batched carbon report audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched carbon report audit"} $code"

    fun weightedTotal(): Double = round((cost - limit.toDouble()) * 100) / 100

    fun snapshot(): BatchedCarbonReportAudit {
        return BatchedCarbonReportAudit(
            code = code,
            name = name,
            externalRef = externalRef,
            label = label,
            cost = cost,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched carbon report audit in status planned. */
fun newBatchedCarbonReportAudit(code: String, name: String): BatchedCarbonReportAudit {
    return BatchedCarbonReportAudit(
        code = code,
        name = name,
        externalRef = "",
        label = "",
        cost = 0.0,
        limit = 0,
        status = BatchedCarbonReportAuditStatus.PLANNED,
    )
}
