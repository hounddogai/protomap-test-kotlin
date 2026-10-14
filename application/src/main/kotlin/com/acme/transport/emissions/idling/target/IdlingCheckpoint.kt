package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling checkpoint is in its life cycle. */
enum class IdlingCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a idling checkpoint and its current status. */
data class IdlingCheckpoint(
    var code: String,
    var label: String,
    var trailerId: String,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var createdAt: Int,
    var vehicleId: String,
    var externalRef: String,
    var stopCount: Int,
    var status: IdlingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingCheckpointStatus): Boolean {
        when (status) {
            IdlingCheckpointStatus.OPEN -> {
                when (next) {
                    IdlingCheckpointStatus.REOPENED, IdlingCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            IdlingCheckpointStatus.IN_REVIEW -> return next == IdlingCheckpointStatus.RESOLVED
            IdlingCheckpointStatus.RESOLVED -> return next == IdlingCheckpointStatus.REOPENED || next == IdlingCheckpointStatus.ARCHIVED
            IdlingCheckpointStatus.REOPENED -> {
                when (next) {
                    IdlingCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("idling checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round(stopCount.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): IdlingCheckpoint {
        return IdlingCheckpoint(
            code = code,
            label = label,
            trailerId = trailerId,
            attributes = attributes,
            notes = notes,
            createdAt = createdAt,
            vehicleId = vehicleId,
            externalRef = externalRef,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling checkpoint in status open. */
fun newIdlingCheckpoint(code: String, label: String): IdlingCheckpoint {
    return IdlingCheckpoint(
        code = code,
        label = label,
        trailerId = "",
        attributes = mutableMapOf<String, String>(),
        notes = "",
        createdAt = 0,
        vehicleId = "",
        externalRef = "",
        stopCount = 0,
        status = IdlingCheckpointStatus.OPEN,
    )
}
