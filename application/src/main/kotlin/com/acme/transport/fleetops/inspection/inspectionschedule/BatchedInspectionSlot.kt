package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched inspection slot is in its life cycle. */
enum class BatchedInspectionSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched inspection slot as the inspection team keeps it. */
data class BatchedInspectionSlot(
    var code: String,
    var title: String,
    var shiftCode: String,
    var discount: Double,
    var capacity: Int,
    var status: BatchedInspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionSlotStatus): Boolean {
        when (status) {
            BatchedInspectionSlotStatus.REQUESTED -> {
                when (next) {
                    BatchedInspectionSlotStatus.GRANTED, BatchedInspectionSlotStatus.REVOKED -> return true
                }
                return false
            }
            BatchedInspectionSlotStatus.GRANTED -> {
                when (next) {
                    BatchedInspectionSlotStatus.REVOKED, BatchedInspectionSlotStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedInspectionSlotStatus.REVOKED -> return next == BatchedInspectionSlotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedInspectionSlotStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched inspection slot"} $code"

    fun effectiveVolume(): Double = round((discount + capacity.toDouble()) * 100) / 100

    fun snapshot(): BatchedInspectionSlot {
        return BatchedInspectionSlot(
            code = code,
            title = title,
            shiftCode = shiftCode,
            discount = discount,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched inspection slot in status requested. */
fun newBatchedInspectionSlot(code: String, title: String): BatchedInspectionSlot {
    return BatchedInspectionSlot(
        code = code,
        title = title,
        shiftCode = "",
        discount = 0.0,
        capacity = 0,
        status = BatchedInspectionSlotStatus.REQUESTED,
    )
}
