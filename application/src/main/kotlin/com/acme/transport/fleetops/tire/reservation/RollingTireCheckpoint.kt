package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tire checkpoint is in its life cycle. */
enum class RollingTireCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling tire checkpoint as the tire team keeps it. */
data class RollingTireCheckpoint(
    var code: String,
    var name: String,
    var archived: Boolean,
    var region: String,
    var retries: Int,
    var label: String,
    var minimum: Double,
    var status: RollingTireCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTireCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTireCheckpointStatus): Boolean {
        when (status) {
            RollingTireCheckpointStatus.DRAFT -> return next == RollingTireCheckpointStatus.ACTIVE || next == RollingTireCheckpointStatus.SUSPENDED
            RollingTireCheckpointStatus.PENDING -> {
                when (next) {
                    RollingTireCheckpointStatus.ACTIVE -> return true
                }
                return false
            }
            RollingTireCheckpointStatus.ACTIVE -> return next == RollingTireCheckpointStatus.SUSPENDED || next == RollingTireCheckpointStatus.CLOSED
            RollingTireCheckpointStatus.SUSPENDED -> {
                when (next) {
                    RollingTireCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTireCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rolling tire checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((retries.toDouble() + minimum) * 100) / 100

    fun snapshot(): RollingTireCheckpoint {
        return RollingTireCheckpoint(
            code = code,
            name = name,
            archived = archived,
            region = region,
            retries = retries,
            label = label,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling tire checkpoint in status draft. */
fun newRollingTireCheckpoint(code: String, name: String): RollingTireCheckpoint {
    return RollingTireCheckpoint(
        code = code,
        name = name,
        archived = false,
        region = "",
        retries = 0,
        label = "",
        minimum = 0.0,
        status = RollingTireCheckpointStatus.DRAFT,
    )
}
