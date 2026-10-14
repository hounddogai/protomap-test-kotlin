package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global inspection batch is in its life cycle. */
enum class GlobalInspectionBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global inspection batch as the inspection team keeps it. */
data class GlobalInspectionBatch(
    var code: String,
    var label: String,
    var vehicleId: String,
    var attempts: Int,
    var discount: Double,
    var phone: String,
    var approved: Boolean,
    var maximum: Double,
    var pallets: Int,
    var expiresAt: Int,
    var status: GlobalInspectionBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionBatchStatus): Boolean {
        when (status) {
            GlobalInspectionBatchStatus.PROPOSED -> return next == GlobalInspectionBatchStatus.APPROVED || next == GlobalInspectionBatchStatus.REJECTED
            GlobalInspectionBatchStatus.APPROVED -> return next == GlobalInspectionBatchStatus.REJECTED || next == GlobalInspectionBatchStatus.WITHDRAWN
            GlobalInspectionBatchStatus.REJECTED -> return next == GlobalInspectionBatchStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalInspectionBatchStatus) {
        if (!canMoveTo(next)) {
            error("global inspection batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global inspection batch"} $code"

    fun adjustedValue(): Double = round((maximum + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalInspectionBatch {
        return GlobalInspectionBatch(
            code = code,
            label = label,
            vehicleId = vehicleId,
            attempts = attempts,
            discount = discount,
            phone = phone,
            approved = approved,
            maximum = maximum,
            pallets = pallets,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global inspection batch in status proposed. */
fun newGlobalInspectionBatch(code: String, label: String): GlobalInspectionBatch {
    return GlobalInspectionBatch(
        code = code,
        label = label,
        vehicleId = "",
        attempts = 0,
        discount = 0.0,
        phone = "",
        approved = false,
        maximum = 0.0,
        pallets = 0,
        expiresAt = 0,
        status = GlobalInspectionBatchStatus.PROPOSED,
    )
}
