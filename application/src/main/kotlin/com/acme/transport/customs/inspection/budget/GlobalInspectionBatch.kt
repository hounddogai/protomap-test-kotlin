package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global inspection batch is in its life cycle. */
enum class GlobalInspectionBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global inspection batch, with what the planners need to act on it. */
data class GlobalInspectionBatch(
    var code: String,
    var name: String,
    var sequence: Int,
    var driverId: String,
    var version: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var quantity: Int,
    var status: GlobalInspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionBatchStatus): Boolean {
        when (status) {
            GlobalInspectionBatchStatus.OPEN -> {
                when (next) {
                    GlobalInspectionBatchStatus.REOPENED -> return true
                }
                return false
            }
            GlobalInspectionBatchStatus.IN_REVIEW -> {
                when (next) {
                    GlobalInspectionBatchStatus.RESOLVED, GlobalInspectionBatchStatus.REOPENED -> return true
                }
                return false
            }
            GlobalInspectionBatchStatus.RESOLVED -> {
                when (next) {
                    GlobalInspectionBatchStatus.REOPENED, GlobalInspectionBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalInspectionBatchStatus.REOPENED -> return next == GlobalInspectionBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalInspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("global inspection batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((quantity.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): GlobalInspectionBatch {
        return GlobalInspectionBatch(
            code = code,
            name = name,
            sequence = sequence,
            driverId = driverId,
            version = version,
            pinned = pinned,
            vehicleId = vehicleId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global inspection batch in status open. */
fun newGlobalInspectionBatch(code: String, name: String): GlobalInspectionBatch {
    return GlobalInspectionBatch(
        code = code,
        name = name,
        sequence = 0,
        driverId = "",
        version = 0,
        pinned = false,
        vehicleId = "",
        quantity = 0,
        status = GlobalInspectionBatchStatus.OPEN,
    )
}
