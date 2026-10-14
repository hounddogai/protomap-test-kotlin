package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted scorecard quota records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a weighted scorecard quota is in its life cycle. */
enum class WeightedScorecardQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted scorecard quota and its current status. */
data class WeightedScorecardQuota(
    var code: String,
    var name: String,
    var depot: String,
    var budget: Double,
    var maximum: Double,
    var segment: String,
    var discount: Double,
    var etaMinutes: Int,
    var externalRef: String,
    var distanceKm: Double,
    var status: WeightedScorecardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedScorecardQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedScorecardQuotaStatus): Boolean {
        when (status) {
            WeightedScorecardQuotaStatus.DRAFT -> {
                when (next) {
                    WeightedScorecardQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedScorecardQuotaStatus.PENDING -> {
                when (next) {
                    WeightedScorecardQuotaStatus.ACTIVE, WeightedScorecardQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedScorecardQuotaStatus.ACTIVE -> {
                when (next) {
                    WeightedScorecardQuotaStatus.SUSPENDED, WeightedScorecardQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedScorecardQuotaStatus.SUSPENDED -> return next == WeightedScorecardQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedScorecardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted scorecard quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted scorecard quota"} $code"

    fun netLoad(): Double = round(discount * maximum * 100) / 100

    fun snapshot(): WeightedScorecardQuota {
        return WeightedScorecardQuota(
            code = code,
            name = name,
            depot = depot,
            budget = budget,
            maximum = maximum,
            segment = segment,
            discount = discount,
            etaMinutes = etaMinutes,
            externalRef = externalRef,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted scorecard quota in status draft. */
fun newWeightedScorecardQuota(code: String, name: String): WeightedScorecardQuota {
    return WeightedScorecardQuota(
        code = code,
        name = name,
        depot = "",
        budget = 0.0,
        maximum = 0.0,
        segment = "",
        discount = 0.0,
        etaMinutes = 0,
        externalRef = "",
        distanceKm = 0.0,
        status = WeightedScorecardQuotaStatus.DRAFT,
    )
}
