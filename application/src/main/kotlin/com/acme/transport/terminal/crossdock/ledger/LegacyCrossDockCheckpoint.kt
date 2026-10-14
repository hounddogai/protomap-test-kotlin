package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy cross dock checkpoint is in its life cycle. */
enum class LegacyCrossDockCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy cross dock checkpoint and its current status. */
data class LegacyCrossDockCheckpoint(
    var code: String,
    var label: String,
    var lane: String,
    var pinned: Boolean,
    var dock: String,
    var archived: Boolean,
    var source: String,
    var startsAt: Int,
    var attempts: Int,
    var ceiling: Double,
    var status: LegacyCrossDockCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockCheckpointStatus): Boolean {
        when (status) {
            LegacyCrossDockCheckpointStatus.DRAFT -> {
                when (next) {
                    LegacyCrossDockCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            LegacyCrossDockCheckpointStatus.PENDING -> return next == LegacyCrossDockCheckpointStatus.SUSPENDED
            LegacyCrossDockCheckpointStatus.ACTIVE -> return next == LegacyCrossDockCheckpointStatus.CLOSED
            LegacyCrossDockCheckpointStatus.SUSPENDED -> return next == LegacyCrossDockCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((startsAt.toDouble() + ceiling) * 100) / 100

    fun snapshot(): LegacyCrossDockCheckpoint {
        return LegacyCrossDockCheckpoint(
            code = code,
            label = label,
            lane = lane,
            pinned = pinned,
            dock = dock,
            archived = archived,
            source = source,
            startsAt = startsAt,
            attempts = attempts,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy cross dock checkpoint in status draft. */
fun newLegacyCrossDockCheckpoint(code: String, label: String): LegacyCrossDockCheckpoint {
    return LegacyCrossDockCheckpoint(
        code = code,
        label = label,
        lane = "",
        pinned = false,
        dock = "",
        archived = false,
        source = "",
        startsAt = 0,
        attempts = 0,
        ceiling = 0.0,
        status = LegacyCrossDockCheckpointStatus.DRAFT,
    )
}
