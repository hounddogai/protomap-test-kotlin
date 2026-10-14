package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched trailer override records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a batched trailer override is in its life cycle. */
enum class BatchedTrailerOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched trailer override as the trailer team keeps it. */
data class BatchedTrailerOverride(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var reviewer: String,
    var discount: Double,
    var sequence: Int,
    var version: Int,
    var status: BatchedTrailerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerOverrideStatus): Boolean {
        when (status) {
            BatchedTrailerOverrideStatus.IDLE -> return next == BatchedTrailerOverrideStatus.WARMING
            BatchedTrailerOverrideStatus.WARMING -> return next == BatchedTrailerOverrideStatus.STOPPED
            BatchedTrailerOverrideStatus.READY -> {
                when (next) {
                    BatchedTrailerOverrideStatus.STOPPED -> return true
                }
                return false
            }
            BatchedTrailerOverrideStatus.DRAINING -> {
                when (next) {
                    BatchedTrailerOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer override $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((version.toDouble() - discount) * 100) / 100

    fun snapshot(): BatchedTrailerOverride {
        return BatchedTrailerOverride(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            reviewer = reviewer,
            discount = discount,
            sequence = sequence,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched trailer override in status idle. */
fun newBatchedTrailerOverride(code: String, name: String): BatchedTrailerOverride {
    return BatchedTrailerOverride(
        code = code,
        name = name,
        durationMinutes = 0,
        reviewer = "",
        discount = 0.0,
        sequence = 0,
        version = 0,
        status = BatchedTrailerOverrideStatus.IDLE,
    )
}
