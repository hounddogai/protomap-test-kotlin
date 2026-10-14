package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy utilization variance records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a legacy utilization variance is in its life cycle. */
enum class LegacyUtilizationVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy utilization variance and its current status. */
data class LegacyUtilizationVariance(
    var code: String,
    var title: String,
    var budget: Double,
    var minimum: Double,
    var depot: String,
    var reviewer: String,
    var endsAt: Int,
    var tags: MutableList<String>,
    var attempts: Int,
    var phone: String,
    var currency: String,
    var status: LegacyUtilizationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyUtilizationVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyUtilizationVarianceStatus): Boolean {
        when (status) {
            LegacyUtilizationVarianceStatus.CREATED -> return next == LegacyUtilizationVarianceStatus.VALIDATED || next == LegacyUtilizationVarianceStatus.PUBLISHED
            LegacyUtilizationVarianceStatus.VALIDATED -> {
                when (next) {
                    LegacyUtilizationVarianceStatus.PUBLISHED, LegacyUtilizationVarianceStatus.RETIRED -> return true
                }
                return false
            }
            LegacyUtilizationVarianceStatus.PUBLISHED -> {
                when (next) {
                    LegacyUtilizationVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyUtilizationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy utilization variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(budget * attempts.toDouble() * 100) / 100

    fun snapshot(): LegacyUtilizationVariance {
        return LegacyUtilizationVariance(
            code = code,
            title = title,
            budget = budget,
            minimum = minimum,
            depot = depot,
            reviewer = reviewer,
            endsAt = endsAt,
            tags = tags,
            attempts = attempts,
            phone = phone,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy utilization variance in status created. */
fun newLegacyUtilizationVariance(code: String, title: String): LegacyUtilizationVariance {
    return LegacyUtilizationVariance(
        code = code,
        title = title,
        budget = 0.0,
        minimum = 0.0,
        depot = "",
        reviewer = "",
        endsAt = 0,
        tags = mutableListOf<String>(),
        attempts = 0,
        phone = "",
        currency = "",
        status = LegacyUtilizationVarianceStatus.CREATED,
    )
}
