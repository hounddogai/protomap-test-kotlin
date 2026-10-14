package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted emission factor threshold is in its life cycle. */
enum class WeightedEmissionFactorThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted emission factor threshold, with what the planners need to act on it. */
data class WeightedEmissionFactorThreshold(
    var code: String,
    var title: String,
    var category: String,
    var locale: String,
    var routeCode: String,
    var updatedAt: Int,
    var summary: String,
    var expiresAt: Int,
    var sequence: Int,
    var status: WeightedEmissionFactorThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEmissionFactorThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEmissionFactorThresholdStatus): Boolean {
        when (status) {
            WeightedEmissionFactorThresholdStatus.CREATED -> {
                when (next) {
                    WeightedEmissionFactorThresholdStatus.PUBLISHED, WeightedEmissionFactorThresholdStatus.RETIRED -> return true
                }
                return false
            }
            WeightedEmissionFactorThresholdStatus.VALIDATED -> {
                when (next) {
                    WeightedEmissionFactorThresholdStatus.RETIRED -> return true
                }
                return false
            }
            WeightedEmissionFactorThresholdStatus.PUBLISHED -> {
                when (next) {
                    WeightedEmissionFactorThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedEmissionFactorThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted emission factor threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedVolume(): Double = round(sequence.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): WeightedEmissionFactorThreshold {
        return WeightedEmissionFactorThreshold(
            code = code,
            title = title,
            category = category,
            locale = locale,
            routeCode = routeCode,
            updatedAt = updatedAt,
            summary = summary,
            expiresAt = expiresAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted emission factor threshold in status created. */
fun newWeightedEmissionFactorThreshold(code: String, title: String): WeightedEmissionFactorThreshold {
    return WeightedEmissionFactorThreshold(
        code = code,
        title = title,
        category = "",
        locale = "",
        routeCode = "",
        updatedAt = 0,
        summary = "",
        expiresAt = 0,
        sequence = 0,
        status = WeightedEmissionFactorThresholdStatus.CREATED,
    )
}
