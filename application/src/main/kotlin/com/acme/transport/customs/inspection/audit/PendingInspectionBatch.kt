package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection batch is in its life cycle. */
enum class PendingInspectionBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending inspection batch. Its code is unique within a tenant. */
data class PendingInspectionBatch(
    var code: String,
    var title: String,
    var checksum: String,
    var driverId: String,
    var name: String,
    var label: String,
    var priority: Int,
    var archived: Boolean,
    var region: String,
    var weight: Double,
    var status: PendingInspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionBatchStatus): Boolean {
        when (status) {
            PendingInspectionBatchStatus.REQUESTED -> {
                when (next) {
                    PendingInspectionBatchStatus.GRANTED, PendingInspectionBatchStatus.EXPIRED -> return true
                }
                return false
            }
            PendingInspectionBatchStatus.GRANTED -> {
                when (next) {
                    PendingInspectionBatchStatus.REVOKED, PendingInspectionBatchStatus.EXPIRED -> return true
                }
                return false
            }
            PendingInspectionBatchStatus.REVOKED -> return next == PendingInspectionBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingInspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending inspection batch"} $code"

    fun netValue(): Double = round((priority.toDouble() + weight) * 100) / 100

    fun snapshot(): PendingInspectionBatch {
        return PendingInspectionBatch(
            code = code,
            title = title,
            checksum = checksum,
            driverId = driverId,
            name = name,
            label = label,
            priority = priority,
            archived = archived,
            region = region,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending inspection batch in status requested. */
fun newPendingInspectionBatch(code: String, title: String): PendingInspectionBatch {
    return PendingInspectionBatch(
        code = code,
        title = title,
        checksum = "",
        driverId = "",
        name = "",
        label = "",
        priority = 0,
        archived = false,
        region = "",
        weight = 0.0,
        status = PendingInspectionBatchStatus.REQUESTED,
    )
}
