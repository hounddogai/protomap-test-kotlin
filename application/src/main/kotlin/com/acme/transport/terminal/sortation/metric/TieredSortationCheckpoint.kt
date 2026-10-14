package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered sortation checkpoint is in its life cycle. */
enum class TieredSortationCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered sortation checkpoint and its current status. */
data class TieredSortationCheckpoint(
    var code: String,
    var name: String,
    var timezone: String,
    var rate: Double,
    var margin: Double,
    var externalRef: String,
    var floorValue: Double,
    var status: TieredSortationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationCheckpointStatus): Boolean {
        when (status) {
            TieredSortationCheckpointStatus.REQUESTED -> {
                when (next) {
                    TieredSortationCheckpointStatus.REVOKED, TieredSortationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            TieredSortationCheckpointStatus.GRANTED -> return next == TieredSortationCheckpointStatus.EXPIRED
            TieredSortationCheckpointStatus.REVOKED -> {
                when (next) {
                    TieredSortationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSortationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered sortation checkpoint"} $code"

    fun effectiveValue(): Double = round((margin + floorValue) * 100) / 100

    fun snapshot(): TieredSortationCheckpoint {
        return TieredSortationCheckpoint(
            code = code,
            name = name,
            timezone = timezone,
            rate = rate,
            margin = margin,
            externalRef = externalRef,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered sortation checkpoint in status requested. */
fun newTieredSortationCheckpoint(code: String, name: String): TieredSortationCheckpoint {
    return TieredSortationCheckpoint(
        code = code,
        name = name,
        timezone = "",
        rate = 0.0,
        margin = 0.0,
        externalRef = "",
        floorValue = 0.0,
        status = TieredSortationCheckpointStatus.REQUESTED,
    )
}
