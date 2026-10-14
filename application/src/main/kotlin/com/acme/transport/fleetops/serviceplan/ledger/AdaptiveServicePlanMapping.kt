package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive service plan mapping is in its life cycle. */
enum class AdaptiveServicePlanMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive service plan mapping and its current status. */
data class AdaptiveServicePlanMapping(
    var code: String,
    var label: String,
    var segment: String,
    var retries: Int,
    var routeCode: String,
    var budget: Double,
    var title: String,
    var status: AdaptiveServicePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServicePlanMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServicePlanMappingStatus): Boolean {
        when (status) {
            AdaptiveServicePlanMappingStatus.PLANNED -> return next == AdaptiveServicePlanMappingStatus.SCHEDULED || next == AdaptiveServicePlanMappingStatus.IN_PROGRESS
            AdaptiveServicePlanMappingStatus.SCHEDULED -> return next == AdaptiveServicePlanMappingStatus.IN_PROGRESS
            AdaptiveServicePlanMappingStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveServicePlanMappingStatus.COMPLETED, AdaptiveServicePlanMappingStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveServicePlanMappingStatus.COMPLETED -> return next == AdaptiveServicePlanMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveServicePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service plan mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive service plan mapping"} $code"

    fun projectedVolume(): Double = round(budget * retries.toDouble() * 100) / 100

    fun snapshot(): AdaptiveServicePlanMapping {
        return AdaptiveServicePlanMapping(
            code = code,
            label = label,
            segment = segment,
            retries = retries,
            routeCode = routeCode,
            budget = budget,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive service plan mapping in status planned. */
fun newAdaptiveServicePlanMapping(code: String, label: String): AdaptiveServicePlanMapping {
    return AdaptiveServicePlanMapping(
        code = code,
        label = label,
        segment = "",
        retries = 0,
        routeCode = "",
        budget = 0.0,
        title = "",
        status = AdaptiveServicePlanMappingStatus.PLANNED,
    )
}
