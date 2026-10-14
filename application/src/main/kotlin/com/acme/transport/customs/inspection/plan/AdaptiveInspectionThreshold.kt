package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection threshold is in its life cycle. */
enum class AdaptiveInspectionThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive inspection threshold, with what the planners need to act on it. */
data class AdaptiveInspectionThreshold(
    var code: String,
    var title: String,
    var budget: Double,
    var routeCode: String,
    var retries: Int,
    var attributes: MutableMap<String, String>,
    var odometer: Int,
    var graceDays: Int,
    var capacity: Int,
    var status: AdaptiveInspectionThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionThresholdStatus): Boolean {
        when (status) {
            AdaptiveInspectionThresholdStatus.REQUESTED -> return next == AdaptiveInspectionThresholdStatus.GRANTED || next == AdaptiveInspectionThresholdStatus.REVOKED
            AdaptiveInspectionThresholdStatus.GRANTED -> return next == AdaptiveInspectionThresholdStatus.EXPIRED
            AdaptiveInspectionThresholdStatus.REVOKED -> return next == AdaptiveInspectionThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((graceDays.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveInspectionThreshold {
        return AdaptiveInspectionThreshold(
            code = code,
            title = title,
            budget = budget,
            routeCode = routeCode,
            retries = retries,
            attributes = attributes,
            odometer = odometer,
            graceDays = graceDays,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive inspection threshold in status requested. */
fun newAdaptiveInspectionThreshold(code: String, title: String): AdaptiveInspectionThreshold {
    return AdaptiveInspectionThreshold(
        code = code,
        title = title,
        budget = 0.0,
        routeCode = "",
        retries = 0,
        attributes = mutableMapOf<String, String>(),
        odometer = 0,
        graceDays = 0,
        capacity = 0,
        status = AdaptiveInspectionThresholdStatus.REQUESTED,
    )
}
