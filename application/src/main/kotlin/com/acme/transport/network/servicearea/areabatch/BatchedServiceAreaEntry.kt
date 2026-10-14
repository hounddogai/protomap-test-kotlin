package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service area entry is in its life cycle. */
enum class BatchedServiceAreaEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched service area entry. Its code is unique within a tenant. */
data class BatchedServiceAreaEntry(
    var code: String,
    var label: String,
    var segment: String,
    var currency: String,
    var limit: Int,
    var maximum: Double,
    var batchSize: Int,
    var pinned: Boolean,
    var durationMinutes: Int,
    var status: BatchedServiceAreaEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServiceAreaEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServiceAreaEntryStatus): Boolean {
        when (status) {
            BatchedServiceAreaEntryStatus.PLANNED -> {
                when (next) {
                    BatchedServiceAreaEntryStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedServiceAreaEntryStatus.SCHEDULED -> {
                when (next) {
                    BatchedServiceAreaEntryStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedServiceAreaEntryStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedServiceAreaEntryStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedServiceAreaEntryStatus.COMPLETED -> {
                when (next) {
                    BatchedServiceAreaEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedServiceAreaEntryStatus) {
        if (!canMoveTo(next)) {
            error("batched service area entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round(batchSize.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): BatchedServiceAreaEntry {
        return BatchedServiceAreaEntry(
            code = code,
            label = label,
            segment = segment,
            currency = currency,
            limit = limit,
            maximum = maximum,
            batchSize = batchSize,
            pinned = pinned,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched service area entry in status planned. */
fun newBatchedServiceAreaEntry(code: String, label: String): BatchedServiceAreaEntry {
    return BatchedServiceAreaEntry(
        code = code,
        label = label,
        segment = "",
        currency = "",
        limit = 0,
        maximum = 0.0,
        batchSize = 0,
        pinned = false,
        durationMinutes = 0,
        status = BatchedServiceAreaEntryStatus.PLANNED,
    )
}
