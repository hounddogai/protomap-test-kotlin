package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched capacity batch is in its life cycle. */
enum class BatchedCapacityBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched capacity batch. Its code is unique within a tenant. */
data class BatchedCapacityBatch(
    var code: String,
    var title: String,
    var version: Int,
    var fuelLiters: Double,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var summary: String,
    var etaMinutes: Int,
    var driverId: String,
    var region: String,
    var status: BatchedCapacityBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityBatchStatus): Boolean {
        when (status) {
            BatchedCapacityBatchStatus.IDLE -> return next == BatchedCapacityBatchStatus.READY || next == BatchedCapacityBatchStatus.STOPPED
            BatchedCapacityBatchStatus.WARMING -> return next == BatchedCapacityBatchStatus.STOPPED
            BatchedCapacityBatchStatus.READY -> return next == BatchedCapacityBatchStatus.DRAINING || next == BatchedCapacityBatchStatus.STOPPED
            BatchedCapacityBatchStatus.DRAINING -> return next == BatchedCapacityBatchStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedCapacityBatchStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((floorValue + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): BatchedCapacityBatch {
        return BatchedCapacityBatch(
            code = code,
            title = title,
            version = version,
            fuelLiters = fuelLiters,
            floorValue = floorValue,
            attributes = attributes,
            pallets = pallets,
            summary = summary,
            etaMinutes = etaMinutes,
            driverId = driverId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched capacity batch in status idle. */
fun newBatchedCapacityBatch(code: String, title: String): BatchedCapacityBatch {
    return BatchedCapacityBatch(
        code = code,
        title = title,
        version = 0,
        fuelLiters = 0.0,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        summary = "",
        etaMinutes = 0,
        driverId = "",
        region = "",
        status = BatchedCapacityBatchStatus.IDLE,
    )
}
