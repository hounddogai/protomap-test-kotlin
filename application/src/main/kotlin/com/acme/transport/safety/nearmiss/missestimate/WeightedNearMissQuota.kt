package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted near miss quota is in its life cycle. */
enum class WeightedNearMissQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted near miss quota and its current status. */
data class WeightedNearMissQuota(
    var code: String,
    var name: String,
    var priority: Int,
    var weight: Double,
    var amount: Double,
    var carrier: String,
    var timezone: String,
    var pinned: Boolean,
    var status: WeightedNearMissQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedNearMissQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedNearMissQuotaStatus): Boolean {
        when (status) {
            WeightedNearMissQuotaStatus.DRAFT -> return next == WeightedNearMissQuotaStatus.ACTIVE || next == WeightedNearMissQuotaStatus.SUSPENDED
            WeightedNearMissQuotaStatus.PENDING -> {
                when (next) {
                    WeightedNearMissQuotaStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedNearMissQuotaStatus.ACTIVE -> {
                when (next) {
                    WeightedNearMissQuotaStatus.SUSPENDED, WeightedNearMissQuotaStatus.CLOSED -> return true
                }
                return false
            }
            WeightedNearMissQuotaStatus.SUSPENDED -> return next == WeightedNearMissQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedNearMissQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted near miss quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round((amount - weight) * 100) / 100

    fun snapshot(): WeightedNearMissQuota {
        return WeightedNearMissQuota(
            code = code,
            name = name,
            priority = priority,
            weight = weight,
            amount = amount,
            carrier = carrier,
            timezone = timezone,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted near miss quota in status draft. */
fun newWeightedNearMissQuota(code: String, name: String): WeightedNearMissQuota {
    return WeightedNearMissQuota(
        code = code,
        name = name,
        priority = 0,
        weight = 0.0,
        amount = 0.0,
        carrier = "",
        timezone = "",
        pinned = false,
        status = WeightedNearMissQuotaStatus.DRAFT,
    )
}
