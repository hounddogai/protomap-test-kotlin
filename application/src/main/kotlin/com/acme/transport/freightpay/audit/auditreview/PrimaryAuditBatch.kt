package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary audit batch is in its life cycle. */
enum class PrimaryAuditBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary audit batch, with what the planners need to act on it. */
data class PrimaryAuditBatch(
    var code: String,
    var label: String,
    var dock: String,
    var revision: Int,
    var vehicleId: String,
    var routeCode: String,
    var pinned: Boolean,
    var stopCount: Int,
    var status: PrimaryAuditBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAuditBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAuditBatchStatus): Boolean {
        when (status) {
            PrimaryAuditBatchStatus.REQUESTED -> return next == PrimaryAuditBatchStatus.REVOKED || next == PrimaryAuditBatchStatus.EXPIRED
            PrimaryAuditBatchStatus.GRANTED -> {
                when (next) {
                    PrimaryAuditBatchStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryAuditBatchStatus.REVOKED -> {
                when (next) {
                    PrimaryAuditBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAuditBatchStatus) {
        if (!canMoveTo(next)) {
            error("primary audit batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round(stopCount.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): PrimaryAuditBatch {
        return PrimaryAuditBatch(
            code = code,
            label = label,
            dock = dock,
            revision = revision,
            vehicleId = vehicleId,
            routeCode = routeCode,
            pinned = pinned,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary audit batch in status requested. */
fun newPrimaryAuditBatch(code: String, label: String): PrimaryAuditBatch {
    return PrimaryAuditBatch(
        code = code,
        label = label,
        dock = "",
        revision = 0,
        vehicleId = "",
        routeCode = "",
        pinned = false,
        stopCount = 0,
        status = PrimaryAuditBatchStatus.REQUESTED,
    )
}
