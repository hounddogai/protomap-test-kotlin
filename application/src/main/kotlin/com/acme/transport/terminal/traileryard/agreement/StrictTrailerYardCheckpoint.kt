package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer yard checkpoint is in its life cycle. */
enum class StrictTrailerYardCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict trailer yard checkpoint, with what the planners need to act on it. */
data class StrictTrailerYardCheckpoint(
    var code: String,
    var title: String,
    var discount: Double,
    var carrier: String,
    var etaMinutes: Int,
    var phone: String,
    var sequence: Int,
    var priority: Int,
    var batchSize: Int,
    var lane: String,
    var summary: String,
    var status: StrictTrailerYardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerYardCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerYardCheckpointStatus): Boolean {
        when (status) {
            StrictTrailerYardCheckpointStatus.PROPOSED -> {
                when (next) {
                    StrictTrailerYardCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            StrictTrailerYardCheckpointStatus.APPROVED -> {
                when (next) {
                    StrictTrailerYardCheckpointStatus.REJECTED, StrictTrailerYardCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictTrailerYardCheckpointStatus.REJECTED -> return next == StrictTrailerYardCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictTrailerYardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer yard checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveVolume(): Double = round((batchSize.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): StrictTrailerYardCheckpoint {
        return StrictTrailerYardCheckpoint(
            code = code,
            title = title,
            discount = discount,
            carrier = carrier,
            etaMinutes = etaMinutes,
            phone = phone,
            sequence = sequence,
            priority = priority,
            batchSize = batchSize,
            lane = lane,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict trailer yard checkpoint in status proposed. */
fun newStrictTrailerYardCheckpoint(code: String, title: String): StrictTrailerYardCheckpoint {
    return StrictTrailerYardCheckpoint(
        code = code,
        title = title,
        discount = 0.0,
        carrier = "",
        etaMinutes = 0,
        phone = "",
        sequence = 0,
        priority = 0,
        batchSize = 0,
        lane = "",
        summary = "",
        status = StrictTrailerYardCheckpointStatus.PROPOSED,
    )
}
