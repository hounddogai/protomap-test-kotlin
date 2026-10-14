package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer checkpoint is in its life cycle. */
enum class RollingTrailerCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling trailer checkpoint as the trailer team keeps it. */
data class RollingTrailerCheckpoint(
    var code: String,
    var name: String,
    var maximum: Double,
    var ownerId: String,
    var archived: Boolean,
    var vehicleId: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var threshold: Double,
    var channel: String,
    var status: RollingTrailerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerCheckpointStatus): Boolean {
        when (status) {
            RollingTrailerCheckpointStatus.CREATED -> {
                when (next) {
                    RollingTrailerCheckpointStatus.VALIDATED, RollingTrailerCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingTrailerCheckpointStatus.VALIDATED -> {
                when (next) {
                    RollingTrailerCheckpointStatus.PUBLISHED, RollingTrailerCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            RollingTrailerCheckpointStatus.PUBLISHED -> {
                when (next) {
                    RollingTrailerCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTrailerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round(maximum * threshold * 100) / 100

    fun snapshot(): RollingTrailerCheckpoint {
        return RollingTrailerCheckpoint(
            code = code,
            name = name,
            maximum = maximum,
            ownerId = ownerId,
            archived = archived,
            vehicleId = vehicleId,
            sequence = sequence,
            tags = tags,
            threshold = threshold,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling trailer checkpoint in status created. */
fun newRollingTrailerCheckpoint(code: String, name: String): RollingTrailerCheckpoint {
    return RollingTrailerCheckpoint(
        code = code,
        name = name,
        maximum = 0.0,
        ownerId = "",
        archived = false,
        vehicleId = "",
        sequence = 0,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        channel = "",
        status = RollingTrailerCheckpointStatus.CREATED,
    )
}
