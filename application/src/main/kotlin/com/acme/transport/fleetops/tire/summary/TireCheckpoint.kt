package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire checkpoint is in its life cycle. */
enum class TireCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tire checkpoint, with what the planners need to act on it. */
data class TireCheckpoint(
    var code: String,
    var label: String,
    var limit: Int,
    var timezone: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var archived: Boolean,
    var batchSize: Int,
    var cost: Double,
    var carrier: String,
    var status: TireCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireCheckpointStatus): Boolean {
        when (status) {
            TireCheckpointStatus.OPEN -> {
                when (next) {
                    TireCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            TireCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    TireCheckpointStatus.RESOLVED, TireCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            TireCheckpointStatus.RESOLVED -> {
                when (next) {
                    TireCheckpointStatus.REOPENED, TireCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            TireCheckpointStatus.REOPENED -> return next == TireCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TireCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tire checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire checkpoint"} $code"

    fun netTotal(): Double = round((limit.toDouble() - cost) * 100) / 100

    fun snapshot(): TireCheckpoint {
        return TireCheckpoint(
            code = code,
            label = label,
            limit = limit,
            timezone = timezone,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            archived = archived,
            batchSize = batchSize,
            cost = cost,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire checkpoint in status open. */
fun newTireCheckpoint(code: String, label: String): TireCheckpoint {
    return TireCheckpoint(
        code = code,
        label = label,
        limit = 0,
        timezone = "",
        durationMinutes = 0,
        loadKg = 0.0,
        archived = false,
        batchSize = 0,
        cost = 0.0,
        carrier = "",
        status = TireCheckpointStatus.OPEN,
    )
}
