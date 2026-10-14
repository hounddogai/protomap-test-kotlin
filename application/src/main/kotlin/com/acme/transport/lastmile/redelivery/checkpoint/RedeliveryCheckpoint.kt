package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery checkpoint records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery checkpoint is in its life cycle. */
enum class RedeliveryCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One redelivery checkpoint, with what the planners need to act on it. */
data class RedeliveryCheckpoint(
    var code: String,
    var title: String,
    var graceDays: Int,
    var durationMinutes: Int,
    var ratio: Double,
    var minimum: Double,
    var externalRef: String,
    var createdAt: Int,
    var amount: Double,
    var lane: String,
    var status: RedeliveryCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryCheckpointStatus): Boolean {
        when (status) {
            RedeliveryCheckpointStatus.CREATED -> return next == RedeliveryCheckpointStatus.RETIRED
            RedeliveryCheckpointStatus.VALIDATED -> return next == RedeliveryCheckpointStatus.PUBLISHED || next == RedeliveryCheckpointStatus.RETIRED
            RedeliveryCheckpointStatus.PUBLISHED -> return next == RedeliveryCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RedeliveryCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("redelivery checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"redelivery checkpoint"} $code"

    fun adjustedVolume(): Double = round((amount + minimum) * 100) / 100

    fun snapshot(): RedeliveryCheckpoint {
        return RedeliveryCheckpoint(
            code = code,
            title = title,
            graceDays = graceDays,
            durationMinutes = durationMinutes,
            ratio = ratio,
            minimum = minimum,
            externalRef = externalRef,
            createdAt = createdAt,
            amount = amount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a redelivery checkpoint in status created. */
fun newRedeliveryCheckpoint(code: String, title: String): RedeliveryCheckpoint {
    return RedeliveryCheckpoint(
        code = code,
        title = title,
        graceDays = 0,
        durationMinutes = 0,
        ratio = 0.0,
        minimum = 0.0,
        externalRef = "",
        createdAt = 0,
        amount = 0.0,
        lane = "",
        status = RedeliveryCheckpointStatus.CREATED,
    )
}
