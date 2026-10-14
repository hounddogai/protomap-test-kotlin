package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted emission factor batch is in its life cycle. */
enum class WeightedEmissionFactorBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One weighted emission factor batch, with what the planners need to act on it. */
data class WeightedEmissionFactorBatch(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var approved: Boolean,
    var startsAt: Int,
    var distanceKm: Double,
    var status: WeightedEmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEmissionFactorBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEmissionFactorBatchStatus): Boolean {
        when (status) {
            WeightedEmissionFactorBatchStatus.NEW -> {
                when (next) {
                    WeightedEmissionFactorBatchStatus.DONE, WeightedEmissionFactorBatchStatus.FAILED -> return true
                }
                return false
            }
            WeightedEmissionFactorBatchStatus.QUEUED -> return next == WeightedEmissionFactorBatchStatus.DONE || next == WeightedEmissionFactorBatchStatus.FAILED
            WeightedEmissionFactorBatchStatus.RUNNING -> {
                when (next) {
                    WeightedEmissionFactorBatchStatus.DONE -> return true
                }
                return false
            }
            WeightedEmissionFactorBatchStatus.DONE -> return next == WeightedEmissionFactorBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedEmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("weighted emission factor batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((etaMinutes.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedEmissionFactorBatch {
        return WeightedEmissionFactorBatch(
            code = code,
            name = name,
            pinned = pinned,
            etaMinutes = etaMinutes,
            approved = approved,
            startsAt = startsAt,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted emission factor batch in status new. */
fun newWeightedEmissionFactorBatch(code: String, name: String): WeightedEmissionFactorBatch {
    return WeightedEmissionFactorBatch(
        code = code,
        name = name,
        pinned = false,
        etaMinutes = 0,
        approved = false,
        startsAt = 0,
        distanceKm = 0.0,
        status = WeightedEmissionFactorBatchStatus.NEW,
    )
}
