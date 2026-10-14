package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional service area estimate is in its life cycle. */
enum class RegionalServiceAreaEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One regional service area estimate, with what the planners need to act on it. */
data class RegionalServiceAreaEstimate(
    var code: String,
    var label: String,
    var margin: Double,
    var weight: Double,
    var trailerId: String,
    var country: String,
    var region: String,
    var externalRef: String,
    var capacity: Int,
    var retries: Int,
    var status: RegionalServiceAreaEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaEstimateStatus): Boolean {
        when (status) {
            RegionalServiceAreaEstimateStatus.PROPOSED -> {
                when (next) {
                    RegionalServiceAreaEstimateStatus.APPROVED -> return true
                }
                return false
            }
            RegionalServiceAreaEstimateStatus.APPROVED -> {
                when (next) {
                    RegionalServiceAreaEstimateStatus.REJECTED -> return true
                }
                return false
            }
            RegionalServiceAreaEstimateStatus.REJECTED -> {
                when (next) {
                    RegionalServiceAreaEstimateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaEstimateStatus) {
        if (!canMoveTo(next)) {
            error("regional service area estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((margin - capacity.toDouble()) * 100) / 100

    fun snapshot(): RegionalServiceAreaEstimate {
        return RegionalServiceAreaEstimate(
            code = code,
            label = label,
            margin = margin,
            weight = weight,
            trailerId = trailerId,
            country = country,
            region = region,
            externalRef = externalRef,
            capacity = capacity,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional service area estimate in status proposed. */
fun newRegionalServiceAreaEstimate(code: String, label: String): RegionalServiceAreaEstimate {
    return RegionalServiceAreaEstimate(
        code = code,
        label = label,
        margin = 0.0,
        weight = 0.0,
        trailerId = "",
        country = "",
        region = "",
        externalRef = "",
        capacity = 0,
        retries = 0,
        status = RegionalServiceAreaEstimateStatus.PROPOSED,
    )
}
