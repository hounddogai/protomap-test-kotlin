package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rate card quota is in its life cycle. */
enum class WeightedRateCardQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted rate card quota. Its code is unique within a tenant. */
data class WeightedRateCardQuota(
    var code: String,
    var name: String,
    var capacity: Int,
    var stopCount: Int,
    var source: String,
    var timezone: String,
    var description: String,
    var attempts: Int,
    var cost: Double,
    var category: String,
    var status: WeightedRateCardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRateCardQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRateCardQuotaStatus): Boolean {
        when (status) {
            WeightedRateCardQuotaStatus.CREATED -> return next == WeightedRateCardQuotaStatus.PUBLISHED || next == WeightedRateCardQuotaStatus.RETIRED
            WeightedRateCardQuotaStatus.VALIDATED -> return next == WeightedRateCardQuotaStatus.PUBLISHED
            WeightedRateCardQuotaStatus.PUBLISHED -> {
                when (next) {
                    WeightedRateCardQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRateCardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted rate card quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round(attempts.toDouble() * cost * 100) / 100

    fun snapshot(): WeightedRateCardQuota {
        return WeightedRateCardQuota(
            code = code,
            name = name,
            capacity = capacity,
            stopCount = stopCount,
            source = source,
            timezone = timezone,
            description = description,
            attempts = attempts,
            cost = cost,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted rate card quota in status created. */
fun newWeightedRateCardQuota(code: String, name: String): WeightedRateCardQuota {
    return WeightedRateCardQuota(
        code = code,
        name = name,
        capacity = 0,
        stopCount = 0,
        source = "",
        timezone = "",
        description = "",
        attempts = 0,
        cost = 0.0,
        category = "",
        status = WeightedRateCardQuotaStatus.CREATED,
    )
}
