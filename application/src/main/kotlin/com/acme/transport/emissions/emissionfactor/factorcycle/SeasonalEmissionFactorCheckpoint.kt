package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal emission factor checkpoint is in its life cycle. */
enum class SeasonalEmissionFactorCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal emission factor checkpoint, with what the planners need to act on it. */
data class SeasonalEmissionFactorCheckpoint(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var amount: Double,
    var lane: String,
    var endsAt: Int,
    var status: SeasonalEmissionFactorCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorCheckpointStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorCheckpointStatus.OPEN -> return next == SeasonalEmissionFactorCheckpointStatus.IN_REVIEW || next == SeasonalEmissionFactorCheckpointStatus.REOPENED
            SeasonalEmissionFactorCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalEmissionFactorCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalEmissionFactorCheckpointStatus.RESOLVED -> return next == SeasonalEmissionFactorCheckpointStatus.REOPENED || next == SeasonalEmissionFactorCheckpointStatus.ARCHIVED
            SeasonalEmissionFactorCheckpointStatus.REOPENED -> {
                when (next) {
                    SeasonalEmissionFactorCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((endsAt.toDouble() + amount) * 100) / 100

    fun snapshot(): SeasonalEmissionFactorCheckpoint {
        return SeasonalEmissionFactorCheckpoint(
            code = code,
            label = label,
            distanceKm = distanceKm,
            amount = amount,
            lane = lane,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal emission factor checkpoint in status open. */
fun newSeasonalEmissionFactorCheckpoint(code: String, label: String): SeasonalEmissionFactorCheckpoint {
    return SeasonalEmissionFactorCheckpoint(
        code = code,
        label = label,
        distanceKm = 0.0,
        amount = 0.0,
        lane = "",
        endsAt = 0,
        status = SeasonalEmissionFactorCheckpointStatus.OPEN,
    )
}
