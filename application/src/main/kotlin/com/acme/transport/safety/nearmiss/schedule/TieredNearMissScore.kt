package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered near miss score is in its life cycle. */
enum class TieredNearMissScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tiered near miss score, with what the planners need to act on it. */
data class TieredNearMissScore(
    var code: String,
    var name: String,
    var notes: String,
    var weight: Double,
    var rate: Double,
    var cost: Double,
    var distanceKm: Double,
    var budget: Double,
    var pinned: Boolean,
    var trailerId: String,
    var amount: Double,
    var status: TieredNearMissScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNearMissScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNearMissScoreStatus): Boolean {
        when (status) {
            TieredNearMissScoreStatus.NEW -> {
                when (next) {
                    TieredNearMissScoreStatus.QUEUED, TieredNearMissScoreStatus.RUNNING -> return true
                }
                return false
            }
            TieredNearMissScoreStatus.QUEUED -> return next == TieredNearMissScoreStatus.FAILED
            TieredNearMissScoreStatus.RUNNING -> {
                when (next) {
                    TieredNearMissScoreStatus.DONE -> return true
                }
                return false
            }
            TieredNearMissScoreStatus.DONE -> {
                when (next) {
                    TieredNearMissScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredNearMissScoreStatus) {
        if (!canMoveTo(next)) {
            error("tiered near miss score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered near miss score"} $code"

    fun effectiveLoad(): Double = round((budget - weight) * 100) / 100

    fun snapshot(): TieredNearMissScore {
        return TieredNearMissScore(
            code = code,
            name = name,
            notes = notes,
            weight = weight,
            rate = rate,
            cost = cost,
            distanceKm = distanceKm,
            budget = budget,
            pinned = pinned,
            trailerId = trailerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered near miss score in status new. */
fun newTieredNearMissScore(code: String, name: String): TieredNearMissScore {
    return TieredNearMissScore(
        code = code,
        name = name,
        notes = "",
        weight = 0.0,
        rate = 0.0,
        cost = 0.0,
        distanceKm = 0.0,
        budget = 0.0,
        pinned = false,
        trailerId = "",
        amount = 0.0,
        status = TieredNearMissScoreStatus.NEW,
    )
}
