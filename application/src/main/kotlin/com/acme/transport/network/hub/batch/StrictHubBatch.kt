package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict hub batch is in its life cycle. */
enum class StrictHubBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict hub batch, with what the planners need to act on it. */
data class StrictHubBatch(
    var code: String,
    var name: String,
    var unit: String,
    var limit: Int,
    var sequence: Int,
    var attempts: Int,
    var description: String,
    var status: StrictHubBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictHubBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictHubBatchStatus): Boolean {
        when (status) {
            StrictHubBatchStatus.OPEN -> {
                when (next) {
                    StrictHubBatchStatus.RESOLVED, StrictHubBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictHubBatchStatus.IN_REVIEW -> return next == StrictHubBatchStatus.REOPENED
            StrictHubBatchStatus.RESOLVED -> return next == StrictHubBatchStatus.REOPENED || next == StrictHubBatchStatus.ARCHIVED
            StrictHubBatchStatus.REOPENED -> return next == StrictHubBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictHubBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict hub batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(attempts.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): StrictHubBatch {
        return StrictHubBatch(
            code = code,
            name = name,
            unit = unit,
            limit = limit,
            sequence = sequence,
            attempts = attempts,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict hub batch in status open. */
fun newStrictHubBatch(code: String, name: String): StrictHubBatch {
    return StrictHubBatch(
        code = code,
        name = name,
        unit = "",
        limit = 0,
        sequence = 0,
        attempts = 0,
        description = "",
        status = StrictHubBatchStatus.OPEN,
    )
}
