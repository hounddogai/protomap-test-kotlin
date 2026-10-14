package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay checkpoint records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay checkpoint is in its life cycle. */
enum class RelayCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One relay checkpoint, with what the planners need to act on it. */
data class RelayCheckpoint(
    var code: String,
    var title: String,
    var retries: Int,
    var capacity: Int,
    var distanceKm: Double,
    var status: RelayCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayCheckpointStatus): Boolean {
        when (status) {
            RelayCheckpointStatus.NEW -> return next == RelayCheckpointStatus.DONE
            RelayCheckpointStatus.QUEUED -> {
                when (next) {
                    RelayCheckpointStatus.RUNNING, RelayCheckpointStatus.DONE -> return true
                }
                return false
            }
            RelayCheckpointStatus.RUNNING -> {
                when (next) {
                    RelayCheckpointStatus.DONE -> return true
                }
                return false
            }
            RelayCheckpointStatus.DONE -> return next == RelayCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RelayCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("relay checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round((distanceKm - capacity.toDouble()) * 100) / 100

    fun snapshot(): RelayCheckpoint {
        return RelayCheckpoint(
            code = code,
            title = title,
            retries = retries,
            capacity = capacity,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay checkpoint in status new. */
fun newRelayCheckpoint(code: String, title: String): RelayCheckpoint {
    return RelayCheckpoint(
        code = code,
        title = title,
        retries = 0,
        capacity = 0,
        distanceKm = 0.0,
        status = RelayCheckpointStatus.NEW,
    )
}
