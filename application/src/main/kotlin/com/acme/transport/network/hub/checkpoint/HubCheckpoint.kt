package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub checkpoint records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub checkpoint is in its life cycle. */
enum class HubCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A hub checkpoint. Its code is unique within a tenant. */
data class HubCheckpoint(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var trailerId: String,
    var notes: String,
    var name: String,
    var minimum: Double,
    var tags: MutableList<String>,
    var summary: String,
    var locale: String,
    var status: HubCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubCheckpointStatus): Boolean {
        when (status) {
            HubCheckpointStatus.OPEN -> {
                when (next) {
                    HubCheckpointStatus.REOPENED, HubCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            HubCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    HubCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            HubCheckpointStatus.RESOLVED -> {
                when (next) {
                    HubCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            HubCheckpointStatus.REOPENED -> {
                when (next) {
                    HubCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("hub checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub checkpoint"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((minimum - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): HubCheckpoint {
        return HubCheckpoint(
            code = code,
            title = title,
            updatedAt = updatedAt,
            trailerId = trailerId,
            notes = notes,
            name = name,
            minimum = minimum,
            tags = tags,
            summary = summary,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a hub checkpoint in status open. */
fun newHubCheckpoint(code: String, title: String): HubCheckpoint {
    return HubCheckpoint(
        code = code,
        title = title,
        updatedAt = 0,
        trailerId = "",
        notes = "",
        name = "",
        minimum = 0.0,
        tags = mutableListOf<String>(),
        summary = "",
        locale = "",
        status = HubCheckpointStatus.OPEN,
    )
}
