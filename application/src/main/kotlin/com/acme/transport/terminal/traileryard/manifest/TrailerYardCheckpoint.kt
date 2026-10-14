package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard checkpoint is in its life cycle. */
enum class TrailerYardCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a trailer yard checkpoint and its current status. */
data class TrailerYardCheckpoint(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var loadKg: Double,
    var attempts: Int,
    var carrier: String,
    var createdAt: Int,
    var tags: MutableList<String>,
    var status: TrailerYardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardCheckpointStatus): Boolean {
        when (status) {
            TrailerYardCheckpointStatus.PLANNED -> return next == TrailerYardCheckpointStatus.IN_PROGRESS || next == TrailerYardCheckpointStatus.CANCELLED
            TrailerYardCheckpointStatus.SCHEDULED -> {
                when (next) {
                    TrailerYardCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            TrailerYardCheckpointStatus.IN_PROGRESS -> return next == TrailerYardCheckpointStatus.COMPLETED || next == TrailerYardCheckpointStatus.CANCELLED
            TrailerYardCheckpointStatus.COMPLETED -> return next == TrailerYardCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerYardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((attempts.toDouble() - loadKg) * 100) / 100

    fun snapshot(): TrailerYardCheckpoint {
        return TrailerYardCheckpoint(
            code = code,
            label = label,
            pinned = pinned,
            loadKg = loadKg,
            attempts = attempts,
            carrier = carrier,
            createdAt = createdAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer yard checkpoint in status planned. */
fun newTrailerYardCheckpoint(code: String, label: String): TrailerYardCheckpoint {
    return TrailerYardCheckpoint(
        code = code,
        label = label,
        pinned = false,
        loadKg = 0.0,
        attempts = 0,
        carrier = "",
        createdAt = 0,
        tags = mutableListOf<String>(),
        status = TrailerYardCheckpointStatus.PLANNED,
    )
}
