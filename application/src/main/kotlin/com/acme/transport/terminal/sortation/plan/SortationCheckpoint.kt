package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation checkpoint is in its life cycle. */
enum class SortationCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a sortation checkpoint and its current status. */
data class SortationCheckpoint(
    var code: String,
    var name: String,
    var endsAt: Int,
    var trailerId: String,
    var margin: Double,
    var shiftCode: String,
    var createdAt: Int,
    var limit: Int,
    var description: String,
    var locale: String,
    var status: SortationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationCheckpointStatus): Boolean {
        when (status) {
            SortationCheckpointStatus.NEW -> {
                when (next) {
                    SortationCheckpointStatus.DONE -> return true
                }
                return false
            }
            SortationCheckpointStatus.QUEUED -> {
                when (next) {
                    SortationCheckpointStatus.DONE, SortationCheckpointStatus.FAILED -> return true
                }
                return false
            }
            SortationCheckpointStatus.RUNNING -> {
                when (next) {
                    SortationCheckpointStatus.DONE -> return true
                }
                return false
            }
            SortationCheckpointStatus.DONE -> return next == SortationCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SortationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("sortation checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((createdAt.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): SortationCheckpoint {
        return SortationCheckpoint(
            code = code,
            name = name,
            endsAt = endsAt,
            trailerId = trailerId,
            margin = margin,
            shiftCode = shiftCode,
            createdAt = createdAt,
            limit = limit,
            description = description,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a sortation checkpoint in status new. */
fun newSortationCheckpoint(code: String, name: String): SortationCheckpoint {
    return SortationCheckpoint(
        code = code,
        name = name,
        endsAt = 0,
        trailerId = "",
        margin = 0.0,
        shiftCode = "",
        createdAt = 0,
        limit = 0,
        description = "",
        locale = "",
        status = SortationCheckpointStatus.NEW,
    )
}
