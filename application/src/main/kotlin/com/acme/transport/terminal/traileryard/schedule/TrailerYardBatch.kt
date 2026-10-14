package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard batch is in its life cycle. */
enum class TrailerYardBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer yard batch as the trailer yard team keeps it. */
data class TrailerYardBatch(
    var code: String,
    var name: String,
    var minimum: Double,
    var pallets: Int,
    var approved: Boolean,
    var weight: Double,
    var capacity: Int,
    var ceiling: Double,
    var status: TrailerYardBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardBatchStatus): Boolean {
        when (status) {
            TrailerYardBatchStatus.PROPOSED -> return next == TrailerYardBatchStatus.REJECTED || next == TrailerYardBatchStatus.WITHDRAWN
            TrailerYardBatchStatus.APPROVED -> {
                when (next) {
                    TrailerYardBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerYardBatchStatus.REJECTED -> {
                when (next) {
                    TrailerYardBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardBatchStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((pallets.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardBatch {
        return TrailerYardBatch(
            code = code,
            name = name,
            minimum = minimum,
            pallets = pallets,
            approved = approved,
            weight = weight,
            capacity = capacity,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard batch in status proposed. */
fun newTrailerYardBatch(code: String, name: String): TrailerYardBatch {
    return TrailerYardBatch(
        code = code,
        name = name,
        minimum = 0.0,
        pallets = 0,
        approved = false,
        weight = 0.0,
        capacity = 0,
        ceiling = 0.0,
        status = TrailerYardBatchStatus.PROPOSED,
    )
}
