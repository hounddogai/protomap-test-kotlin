package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback scorecard target is in its life cycle. */
enum class FallbackScorecardTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback scorecard target and its current status. */
data class FallbackScorecardTarget(
    var code: String,
    var name: String,
    var startsAt: Int,
    var depot: String,
    var discount: Double,
    var cost: Double,
    var loadKg: Double,
    var currency: String,
    var distanceKm: Double,
    var expiresAt: Int,
    var label: String,
    var status: FallbackScorecardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackScorecardTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackScorecardTargetStatus): Boolean {
        when (status) {
            FallbackScorecardTargetStatus.NEW -> return next == FallbackScorecardTargetStatus.FAILED
            FallbackScorecardTargetStatus.QUEUED -> return next == FallbackScorecardTargetStatus.RUNNING || next == FallbackScorecardTargetStatus.DONE
            FallbackScorecardTargetStatus.RUNNING -> return next == FallbackScorecardTargetStatus.DONE
            FallbackScorecardTargetStatus.DONE -> {
                when (next) {
                    FallbackScorecardTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackScorecardTargetStatus) {
        if (!canMoveTo(next)) {
            error("fallback scorecard target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fallback scorecard target"} $code"

    fun netValue(): Double = round((loadKg - distanceKm) * 100) / 100

    fun snapshot(): FallbackScorecardTarget {
        return FallbackScorecardTarget(
            code = code,
            name = name,
            startsAt = startsAt,
            depot = depot,
            discount = discount,
            cost = cost,
            loadKg = loadKg,
            currency = currency,
            distanceKm = distanceKm,
            expiresAt = expiresAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback scorecard target in status new. */
fun newFallbackScorecardTarget(code: String, name: String): FallbackScorecardTarget {
    return FallbackScorecardTarget(
        code = code,
        name = name,
        startsAt = 0,
        depot = "",
        discount = 0.0,
        cost = 0.0,
        loadKg = 0.0,
        currency = "",
        distanceKm = 0.0,
        expiresAt = 0,
        label = "",
        status = FallbackScorecardTargetStatus.NEW,
    )
}
