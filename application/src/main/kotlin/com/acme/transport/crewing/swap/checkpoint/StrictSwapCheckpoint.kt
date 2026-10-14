package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict swap checkpoint records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a strict swap checkpoint is in its life cycle. */
enum class StrictSwapCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict swap checkpoint as the swap team keeps it. */
data class StrictSwapCheckpoint(
    var code: String,
    var title: String,
    var windowDays: Int,
    var pallets: Int,
    var timezone: String,
    var region: String,
    var expiresAt: Int,
    var graceDays: Int,
    var status: StrictSwapCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSwapCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSwapCheckpointStatus): Boolean {
        when (status) {
            StrictSwapCheckpointStatus.PLANNED -> {
                when (next) {
                    StrictSwapCheckpointStatus.SCHEDULED -> return true
                }
                return false
            }
            StrictSwapCheckpointStatus.SCHEDULED -> {
                when (next) {
                    StrictSwapCheckpointStatus.IN_PROGRESS, StrictSwapCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            StrictSwapCheckpointStatus.IN_PROGRESS -> return next == StrictSwapCheckpointStatus.COMPLETED
            StrictSwapCheckpointStatus.COMPLETED -> {
                when (next) {
                    StrictSwapCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictSwapCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("strict swap checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((pallets.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): StrictSwapCheckpoint {
        return StrictSwapCheckpoint(
            code = code,
            title = title,
            windowDays = windowDays,
            pallets = pallets,
            timezone = timezone,
            region = region,
            expiresAt = expiresAt,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict swap checkpoint in status planned. */
fun newStrictSwapCheckpoint(code: String, title: String): StrictSwapCheckpoint {
    return StrictSwapCheckpoint(
        code = code,
        title = title,
        windowDays = 0,
        pallets = 0,
        timezone = "",
        region = "",
        expiresAt = 0,
        graceDays = 0,
        status = StrictSwapCheckpointStatus.PLANNED,
    )
}
