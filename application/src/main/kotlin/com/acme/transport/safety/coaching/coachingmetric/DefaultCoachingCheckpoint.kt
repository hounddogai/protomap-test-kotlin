package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default coaching checkpoint is in its life cycle. */
enum class DefaultCoachingCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default coaching checkpoint as the coaching team keeps it. */
data class DefaultCoachingCheckpoint(
    var code: String,
    var label: String,
    var rate: Double,
    var trailerId: String,
    var ratio: Double,
    var minimum: Double,
    var retries: Int,
    var checksum: String,
    var active: Boolean,
    var status: DefaultCoachingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingCheckpointStatus): Boolean {
        when (status) {
            DefaultCoachingCheckpointStatus.CREATED -> return next == DefaultCoachingCheckpointStatus.RETIRED
            DefaultCoachingCheckpointStatus.VALIDATED -> {
                when (next) {
                    DefaultCoachingCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            DefaultCoachingCheckpointStatus.PUBLISHED -> {
                when (next) {
                    DefaultCoachingCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCoachingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("default coaching checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((ratio + minimum) * 100) / 100

    fun snapshot(): DefaultCoachingCheckpoint {
        return DefaultCoachingCheckpoint(
            code = code,
            label = label,
            rate = rate,
            trailerId = trailerId,
            ratio = ratio,
            minimum = minimum,
            retries = retries,
            checksum = checksum,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default coaching checkpoint in status created. */
fun newDefaultCoachingCheckpoint(code: String, label: String): DefaultCoachingCheckpoint {
    return DefaultCoachingCheckpoint(
        code = code,
        label = label,
        rate = 0.0,
        trailerId = "",
        ratio = 0.0,
        minimum = 0.0,
        retries = 0,
        checksum = "",
        active = false,
        status = DefaultCoachingCheckpointStatus.CREATED,
    )
}
