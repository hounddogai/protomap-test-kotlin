package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered swap checkpoint is in its life cycle. */
enum class TieredSwapCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered swap checkpoint, with what the planners need to act on it. */
data class TieredSwapCheckpoint(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var description: String,
    var expiresAt: Int,
    var distanceKm: Double,
    var status: TieredSwapCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSwapCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSwapCheckpointStatus): Boolean {
        when (status) {
            TieredSwapCheckpointStatus.CREATED -> return next == TieredSwapCheckpointStatus.PUBLISHED
            TieredSwapCheckpointStatus.VALIDATED -> {
                when (next) {
                    TieredSwapCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            TieredSwapCheckpointStatus.PUBLISHED -> {
                when (next) {
                    TieredSwapCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSwapCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tiered swap checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round(distanceKm * expiresAt.toDouble() * 100) / 100

    fun snapshot(): TieredSwapCheckpoint {
        return TieredSwapCheckpoint(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            description = description,
            expiresAt = expiresAt,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered swap checkpoint in status created. */
fun newTieredSwapCheckpoint(code: String, name: String): TieredSwapCheckpoint {
    return TieredSwapCheckpoint(
        code = code,
        name = name,
        fuelLiters = 0.0,
        description = "",
        expiresAt = 0,
        distanceKm = 0.0,
        status = TieredSwapCheckpointStatus.CREATED,
    )
}
