package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive service plan request is in its life cycle. */
enum class AdaptiveServicePlanRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive service plan request as the service plan team keeps it. */
data class AdaptiveServicePlanRequest(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var segment: String,
    var odometer: Int,
    var category: String,
    var margin: Double,
    var status: AdaptiveServicePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServicePlanRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServicePlanRequestStatus): Boolean {
        when (status) {
            AdaptiveServicePlanRequestStatus.PLANNED -> return next == AdaptiveServicePlanRequestStatus.COMPLETED
            AdaptiveServicePlanRequestStatus.SCHEDULED -> return next == AdaptiveServicePlanRequestStatus.COMPLETED || next == AdaptiveServicePlanRequestStatus.CANCELLED
            AdaptiveServicePlanRequestStatus.IN_PROGRESS -> return next == AdaptiveServicePlanRequestStatus.COMPLETED || next == AdaptiveServicePlanRequestStatus.CANCELLED
            AdaptiveServicePlanRequestStatus.COMPLETED -> {
                when (next) {
                    AdaptiveServicePlanRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveServicePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service plan request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((odometer.toDouble() + margin) * 100) / 100

    fun snapshot(): AdaptiveServicePlanRequest {
        return AdaptiveServicePlanRequest(
            code = code,
            label = label,
            attributes = attributes,
            segment = segment,
            odometer = odometer,
            category = category,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive service plan request in status planned. */
fun newAdaptiveServicePlanRequest(code: String, label: String): AdaptiveServicePlanRequest {
    return AdaptiveServicePlanRequest(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        segment = "",
        odometer = 0,
        category = "",
        margin = 0.0,
        status = AdaptiveServicePlanRequestStatus.PLANNED,
    )
}
