package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending defect batch is in its life cycle. */
enum class PendingDefectBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending defect batch, with what the planners need to act on it. */
data class PendingDefectBatch(
    var code: String,
    var label: String,
    var cost: Double,
    var version: Int,
    var title: String,
    var limit: Int,
    var ownerId: String,
    var dock: String,
    var active: Boolean,
    var graceDays: Int,
    var status: PendingDefectBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDefectBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDefectBatchStatus): Boolean {
        when (status) {
            PendingDefectBatchStatus.PLANNED -> return next == PendingDefectBatchStatus.SCHEDULED || next == PendingDefectBatchStatus.IN_PROGRESS
            PendingDefectBatchStatus.SCHEDULED -> {
                when (next) {
                    PendingDefectBatchStatus.CANCELLED -> return true
                }
                return false
            }
            PendingDefectBatchStatus.IN_PROGRESS -> {
                when (next) {
                    PendingDefectBatchStatus.COMPLETED -> return true
                }
                return false
            }
            PendingDefectBatchStatus.COMPLETED -> {
                when (next) {
                    PendingDefectBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDefectBatchStatus) {
        if (!canMoveTo(next)) {
            error("pending defect batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedTotal(): Double = round((limit.toDouble() + cost) * 100) / 100

    fun snapshot(): PendingDefectBatch {
        return PendingDefectBatch(
            code = code,
            label = label,
            cost = cost,
            version = version,
            title = title,
            limit = limit,
            ownerId = ownerId,
            dock = dock,
            active = active,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending defect batch in status planned. */
fun newPendingDefectBatch(code: String, label: String): PendingDefectBatch {
    return PendingDefectBatch(
        code = code,
        label = label,
        cost = 0.0,
        version = 0,
        title = "",
        limit = 0,
        ownerId = "",
        dock = "",
        active = false,
        graceDays = 0,
        status = PendingDefectBatchStatus.PLANNED,
    )
}
