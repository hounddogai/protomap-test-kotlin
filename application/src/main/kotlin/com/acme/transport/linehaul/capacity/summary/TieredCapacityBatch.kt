package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered capacity batch is in its life cycle. */
enum class TieredCapacityBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered capacity batch and its current status. */
data class TieredCapacityBatch(
    var code: String,
    var title: String,
    var externalRef: String,
    var minimum: Double,
    var segment: String,
    var distanceKm: Double,
    var priority: Int,
    var ownerId: String,
    var status: TieredCapacityBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCapacityBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCapacityBatchStatus): Boolean {
        when (status) {
            TieredCapacityBatchStatus.NEW -> {
                when (next) {
                    TieredCapacityBatchStatus.QUEUED -> return true
                }
                return false
            }
            TieredCapacityBatchStatus.QUEUED -> return next == TieredCapacityBatchStatus.RUNNING
            TieredCapacityBatchStatus.RUNNING -> return next == TieredCapacityBatchStatus.DONE || next == TieredCapacityBatchStatus.FAILED
            TieredCapacityBatchStatus.DONE -> {
                when (next) {
                    TieredCapacityBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCapacityBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered capacity batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered capacity batch"} $code"

    fun projectedLoad(): Double = round((minimum + priority.toDouble()) * 100) / 100

    fun snapshot(): TieredCapacityBatch {
        return TieredCapacityBatch(
            code = code,
            title = title,
            externalRef = externalRef,
            minimum = minimum,
            segment = segment,
            distanceKm = distanceKm,
            priority = priority,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered capacity batch in status new. */
fun newTieredCapacityBatch(code: String, title: String): TieredCapacityBatch {
    return TieredCapacityBatch(
        code = code,
        title = title,
        externalRef = "",
        minimum = 0.0,
        segment = "",
        distanceKm = 0.0,
        priority = 0,
        ownerId = "",
        status = TieredCapacityBatchStatus.NEW,
    )
}
