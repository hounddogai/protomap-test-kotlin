package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global attempt checkpoint is in its life cycle. */
enum class GlobalAttemptCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a global attempt checkpoint and its current status. */
data class GlobalAttemptCheckpoint(
    var code: String,
    var label: String,
    var score: Double,
    var ceiling: Double,
    var category: String,
    var startsAt: Int,
    var status: GlobalAttemptCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAttemptCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAttemptCheckpointStatus): Boolean {
        when (status) {
            GlobalAttemptCheckpointStatus.CREATED -> {
                when (next) {
                    GlobalAttemptCheckpointStatus.VALIDATED, GlobalAttemptCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalAttemptCheckpointStatus.VALIDATED -> {
                when (next) {
                    GlobalAttemptCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            GlobalAttemptCheckpointStatus.PUBLISHED -> return next == GlobalAttemptCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalAttemptCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("global attempt checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((score + ceiling) * 100) / 100

    fun snapshot(): GlobalAttemptCheckpoint {
        return GlobalAttemptCheckpoint(
            code = code,
            label = label,
            score = score,
            ceiling = ceiling,
            category = category,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global attempt checkpoint in status created. */
fun newGlobalAttemptCheckpoint(code: String, label: String): GlobalAttemptCheckpoint {
    return GlobalAttemptCheckpoint(
        code = code,
        label = label,
        score = 0.0,
        ceiling = 0.0,
        category = "",
        startsAt = 0,
        status = GlobalAttemptCheckpointStatus.CREATED,
    )
}
