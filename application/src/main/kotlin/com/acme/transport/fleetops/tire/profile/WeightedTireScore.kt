package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tire score is in its life cycle. */
enum class WeightedTireScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted tire score and its current status. */
data class WeightedTireScore(
    var code: String,
    var label: String,
    var threshold: Double,
    var carrier: String,
    var loadKg: Double,
    var priority: Int,
    var status: WeightedTireScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTireScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTireScoreStatus): Boolean {
        when (status) {
            WeightedTireScoreStatus.DRAFT -> {
                when (next) {
                    WeightedTireScoreStatus.CLOSED -> return true
                }
                return false
            }
            WeightedTireScoreStatus.PENDING -> return next == WeightedTireScoreStatus.ACTIVE || next == WeightedTireScoreStatus.SUSPENDED
            WeightedTireScoreStatus.ACTIVE -> {
                when (next) {
                    WeightedTireScoreStatus.SUSPENDED, WeightedTireScoreStatus.CLOSED -> return true
                }
                return false
            }
            WeightedTireScoreStatus.SUSPENDED -> return next == WeightedTireScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedTireScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted tire score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((loadKg + threshold) * 100) / 100

    fun snapshot(): WeightedTireScore {
        return WeightedTireScore(
            code = code,
            label = label,
            threshold = threshold,
            carrier = carrier,
            loadKg = loadKg,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted tire score in status draft. */
fun newWeightedTireScore(code: String, label: String): WeightedTireScore {
    return WeightedTireScore(
        code = code,
        label = label,
        threshold = 0.0,
        carrier = "",
        loadKg = 0.0,
        priority = 0,
        status = WeightedTireScoreStatus.DRAFT,
    )
}
