package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual violation batch is in its life cycle. */
enum class ManualViolationBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual violation batch. Its code is unique within a tenant. */
data class ManualViolationBatch(
    var code: String,
    var label: String,
    var discount: Double,
    var routeCode: String,
    var archived: Boolean,
    var batchSize: Int,
    var timezone: String,
    var status: ManualViolationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualViolationBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualViolationBatchStatus): Boolean {
        when (status) {
            ManualViolationBatchStatus.DRAFT -> {
                when (next) {
                    ManualViolationBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualViolationBatchStatus.PENDING -> return next == ManualViolationBatchStatus.CLOSED
            ManualViolationBatchStatus.ACTIVE -> return next == ManualViolationBatchStatus.SUSPENDED || next == ManualViolationBatchStatus.CLOSED
            ManualViolationBatchStatus.SUSPENDED -> {
                when (next) {
                    ManualViolationBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualViolationBatchStatus) {
        if (!canMoveTo(next)) {
            error("manual violation batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual violation batch"} $code"

    fun projectedValue(): Double = round(discount * batchSize.toDouble() * 100) / 100

    fun snapshot(): ManualViolationBatch {
        return ManualViolationBatch(
            code = code,
            label = label,
            discount = discount,
            routeCode = routeCode,
            archived = archived,
            batchSize = batchSize,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual violation batch in status draft. */
fun newManualViolationBatch(code: String, label: String): ManualViolationBatch {
    return ManualViolationBatch(
        code = code,
        label = label,
        discount = 0.0,
        routeCode = "",
        archived = false,
        batchSize = 0,
        timezone = "",
        status = ManualViolationBatchStatus.DRAFT,
    )
}
