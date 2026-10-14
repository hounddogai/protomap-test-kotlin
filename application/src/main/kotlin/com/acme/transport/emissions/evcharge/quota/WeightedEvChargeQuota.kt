package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted ev charge quota records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a weighted ev charge quota is in its life cycle. */
enum class WeightedEvChargeQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted ev charge quota. Its code is unique within a tenant. */
data class WeightedEvChargeQuota(
    var code: String,
    var label: String,
    var pallets: Int,
    var threshold: Double,
    var cost: Double,
    var source: String,
    var capacity: Int,
    var distanceKm: Double,
    var status: WeightedEvChargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeQuotaStatus): Boolean {
        when (status) {
            WeightedEvChargeQuotaStatus.PROPOSED -> {
                when (next) {
                    WeightedEvChargeQuotaStatus.REJECTED -> return true
                }
                return false
            }
            WeightedEvChargeQuotaStatus.APPROVED -> return next == WeightedEvChargeQuotaStatus.REJECTED || next == WeightedEvChargeQuotaStatus.WITHDRAWN
            WeightedEvChargeQuotaStatus.REJECTED -> {
                when (next) {
                    WeightedEvChargeQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round(cost * threshold * 100) / 100

    fun snapshot(): WeightedEvChargeQuota {
        return WeightedEvChargeQuota(
            code = code,
            label = label,
            pallets = pallets,
            threshold = threshold,
            cost = cost,
            source = source,
            capacity = capacity,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted ev charge quota in status proposed. */
fun newWeightedEvChargeQuota(code: String, label: String): WeightedEvChargeQuota {
    return WeightedEvChargeQuota(
        code = code,
        label = label,
        pallets = 0,
        threshold = 0.0,
        cost = 0.0,
        source = "",
        capacity = 0,
        distanceKm = 0.0,
        status = WeightedEvChargeQuotaStatus.PROPOSED,
    )
}
