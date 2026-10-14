package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted carbon report claim is in its life cycle. */
enum class WeightedCarbonReportClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted carbon report claim as the carbon report team keeps it. */
data class WeightedCarbonReportClaim(
    var code: String,
    var name: String,
    var stopCount: Int,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var label: String,
    var depot: String,
    var fuelLiters: Double,
    var status: WeightedCarbonReportClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportClaimStatus): Boolean {
        when (status) {
            WeightedCarbonReportClaimStatus.DRAFT -> {
                when (next) {
                    WeightedCarbonReportClaimStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedCarbonReportClaimStatus.PENDING -> return next == WeightedCarbonReportClaimStatus.SUSPENDED || next == WeightedCarbonReportClaimStatus.CLOSED
            WeightedCarbonReportClaimStatus.ACTIVE -> return next == WeightedCarbonReportClaimStatus.SUSPENDED || next == WeightedCarbonReportClaimStatus.CLOSED
            WeightedCarbonReportClaimStatus.SUSPENDED -> return next == WeightedCarbonReportClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((stopCount.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): WeightedCarbonReportClaim {
        return WeightedCarbonReportClaim(
            code = code,
            name = name,
            stopCount = stopCount,
            currency = currency,
            attributes = attributes,
            label = label,
            depot = depot,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted carbon report claim in status draft. */
fun newWeightedCarbonReportClaim(code: String, name: String): WeightedCarbonReportClaim {
    return WeightedCarbonReportClaim(
        code = code,
        name = name,
        stopCount = 0,
        currency = "",
        attributes = mutableMapOf<String, String>(),
        label = "",
        depot = "",
        fuelLiters = 0.0,
        status = WeightedCarbonReportClaimStatus.DRAFT,
    )
}
