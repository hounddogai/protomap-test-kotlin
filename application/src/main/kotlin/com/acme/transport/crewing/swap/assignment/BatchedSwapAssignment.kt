package com.acme.transport.crewing.swap.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched swap assignment records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a batched swap assignment is in its life cycle. */
enum class BatchedSwapAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a batched swap assignment and its current status. */
data class BatchedSwapAssignment(
    var code: String,
    var name: String,
    var segment: String,
    var floorValue: Double,
    var retries: Int,
    var distanceKm: Double,
    var pallets: Int,
    var status: BatchedSwapAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSwapAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSwapAssignmentStatus): Boolean {
        when (status) {
            BatchedSwapAssignmentStatus.IDLE -> {
                when (next) {
                    BatchedSwapAssignmentStatus.READY -> return true
                }
                return false
            }
            BatchedSwapAssignmentStatus.WARMING -> {
                when (next) {
                    BatchedSwapAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedSwapAssignmentStatus.READY -> {
                when (next) {
                    BatchedSwapAssignmentStatus.DRAINING, BatchedSwapAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedSwapAssignmentStatus.DRAINING -> {
                when (next) {
                    BatchedSwapAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedSwapAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("batched swap assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched swap assignment"} $code"

    fun netVolume(): Double = round((retries.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): BatchedSwapAssignment {
        return BatchedSwapAssignment(
            code = code,
            name = name,
            segment = segment,
            floorValue = floorValue,
            retries = retries,
            distanceKm = distanceKm,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched swap assignment in status idle. */
fun newBatchedSwapAssignment(code: String, name: String): BatchedSwapAssignment {
    return BatchedSwapAssignment(
        code = code,
        name = name,
        segment = "",
        floorValue = 0.0,
        retries = 0,
        distanceKm = 0.0,
        pallets = 0,
        status = BatchedSwapAssignmentStatus.IDLE,
    )
}
