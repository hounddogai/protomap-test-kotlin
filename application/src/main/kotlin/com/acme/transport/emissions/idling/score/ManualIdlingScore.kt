package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual idling score records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a manual idling score is in its life cycle. */
enum class ManualIdlingScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a manual idling score and its current status. */
data class ManualIdlingScore(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var threshold: Double,
    var margin: Double,
    var startsAt: Int,
    var cost: Double,
    var maximum: Double,
    var status: ManualIdlingScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingScoreStatus): Boolean {
        when (status) {
            ManualIdlingScoreStatus.PROPOSED -> return next == ManualIdlingScoreStatus.APPROVED || next == ManualIdlingScoreStatus.WITHDRAWN
            ManualIdlingScoreStatus.APPROVED -> return next == ManualIdlingScoreStatus.REJECTED || next == ManualIdlingScoreStatus.WITHDRAWN
            ManualIdlingScoreStatus.REJECTED -> {
                when (next) {
                    ManualIdlingScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIdlingScoreStatus) {
        if (!canMoveTo(next)) {
            error("manual idling score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((startsAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): ManualIdlingScore {
        return ManualIdlingScore(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            threshold = threshold,
            margin = margin,
            startsAt = startsAt,
            cost = cost,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual idling score in status proposed. */
fun newManualIdlingScore(code: String, name: String): ManualIdlingScore {
    return ManualIdlingScore(
        code = code,
        name = name,
        fuelLiters = 0.0,
        threshold = 0.0,
        margin = 0.0,
        startsAt = 0,
        cost = 0.0,
        maximum = 0.0,
        status = ManualIdlingScoreStatus.PROPOSED,
    )
}
