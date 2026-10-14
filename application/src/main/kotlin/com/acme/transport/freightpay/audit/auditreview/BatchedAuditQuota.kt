package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched audit quota is in its life cycle. */
enum class BatchedAuditQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched audit quota and its current status. */
data class BatchedAuditQuota(
    var code: String,
    var label: String,
    var odometer: Int,
    var durationMinutes: Int,
    var createdAt: Int,
    var contactEmail: String,
    var rate: Double,
    var region: String,
    var unit: String,
    var status: BatchedAuditQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAuditQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAuditQuotaStatus): Boolean {
        when (status) {
            BatchedAuditQuotaStatus.NEW -> return next == BatchedAuditQuotaStatus.RUNNING || next == BatchedAuditQuotaStatus.DONE
            BatchedAuditQuotaStatus.QUEUED -> return next == BatchedAuditQuotaStatus.DONE
            BatchedAuditQuotaStatus.RUNNING -> {
                when (next) {
                    BatchedAuditQuotaStatus.FAILED -> return true
                }
                return false
            }
            BatchedAuditQuotaStatus.DONE -> return next == BatchedAuditQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedAuditQuotaStatus) {
        if (!canMoveTo(next)) {
            error("batched audit quota $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((odometer.toDouble() + rate) * 100) / 100

    fun snapshot(): BatchedAuditQuota {
        return BatchedAuditQuota(
            code = code,
            label = label,
            odometer = odometer,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            contactEmail = contactEmail,
            rate = rate,
            region = region,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched audit quota in status new. */
fun newBatchedAuditQuota(code: String, label: String): BatchedAuditQuota {
    return BatchedAuditQuota(
        code = code,
        label = label,
        odometer = 0,
        durationMinutes = 0,
        createdAt = 0,
        contactEmail = "",
        rate = 0.0,
        region = "",
        unit = "",
        status = BatchedAuditQuotaStatus.NEW,
    )
}
