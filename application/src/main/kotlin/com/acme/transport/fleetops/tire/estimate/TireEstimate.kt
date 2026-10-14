package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire estimate records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire estimate is in its life cycle. */
enum class TireEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tire estimate and its current status. */
data class TireEstimate(
    var code: String,
    var label: String,
    var description: String,
    var trailerId: String,
    var expiresAt: Int,
    var source: String,
    var weight: Double,
    var maximum: Double,
    var status: TireEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireEstimateStatus): Boolean {
        when (status) {
            TireEstimateStatus.PROPOSED -> return next == TireEstimateStatus.REJECTED || next == TireEstimateStatus.WITHDRAWN
            TireEstimateStatus.APPROVED -> {
                when (next) {
                    TireEstimateStatus.REJECTED, TireEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
            TireEstimateStatus.REJECTED -> {
                when (next) {
                    TireEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireEstimateStatus) {
        if (!canMoveTo(next)) {
            error("tire estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((expiresAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): TireEstimate {
        return TireEstimate(
            code = code,
            label = label,
            description = description,
            trailerId = trailerId,
            expiresAt = expiresAt,
            source = source,
            weight = weight,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tire estimate in status proposed. */
fun newTireEstimate(code: String, label: String): TireEstimate {
    return TireEstimate(
        code = code,
        label = label,
        description = "",
        trailerId = "",
        expiresAt = 0,
        source = "",
        weight = 0.0,
        maximum = 0.0,
        status = TireEstimateStatus.PROPOSED,
    )
}
