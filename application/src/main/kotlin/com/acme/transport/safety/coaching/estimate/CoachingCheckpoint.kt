package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching checkpoint is in its life cycle. */
enum class CoachingCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One coaching checkpoint, with what the planners need to act on it. */
data class CoachingCheckpoint(
    var code: String,
    var name: String,
    var minimum: Double,
    var quantity: Int,
    var depot: String,
    var status: CoachingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingCheckpointStatus): Boolean {
        when (status) {
            CoachingCheckpointStatus.CREATED -> {
                when (next) {
                    CoachingCheckpointStatus.VALIDATED -> return true
                }
                return false
            }
            CoachingCheckpointStatus.VALIDATED -> return next == CoachingCheckpointStatus.PUBLISHED || next == CoachingCheckpointStatus.RETIRED
            CoachingCheckpointStatus.PUBLISHED -> return next == CoachingCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CoachingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("coaching checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching checkpoint"} $code"

    fun netTotal(): Double = round((quantity.toDouble() + minimum) * 100) / 100

    fun snapshot(): CoachingCheckpoint {
        return CoachingCheckpoint(
            code = code,
            name = name,
            minimum = minimum,
            quantity = quantity,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a coaching checkpoint in status created. */
fun newCoachingCheckpoint(code: String, name: String): CoachingCheckpoint {
    return CoachingCheckpoint(
        code = code,
        name = name,
        minimum = 0.0,
        quantity = 0,
        depot = "",
        status = CoachingCheckpointStatus.CREATED,
    )
}
