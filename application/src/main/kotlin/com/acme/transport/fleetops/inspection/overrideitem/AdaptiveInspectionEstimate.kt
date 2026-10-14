package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection estimate is in its life cycle. */
enum class AdaptiveInspectionEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive inspection estimate as the inspection team keeps it. */
data class AdaptiveInspectionEstimate(
    var code: String,
    var title: String,
    var region: String,
    var threshold: Double,
    var margin: Double,
    var attempts: Int,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var currency: String,
    var category: String,
    var windowDays: Int,
    var status: AdaptiveInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionEstimateStatus): Boolean {
        when (status) {
            AdaptiveInspectionEstimateStatus.NEW -> return next == AdaptiveInspectionEstimateStatus.DONE
            AdaptiveInspectionEstimateStatus.QUEUED -> {
                when (next) {
                    AdaptiveInspectionEstimateStatus.RUNNING, AdaptiveInspectionEstimateStatus.DONE -> return true
                }
                return false
            }
            AdaptiveInspectionEstimateStatus.RUNNING -> {
                when (next) {
                    AdaptiveInspectionEstimateStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveInspectionEstimateStatus.DONE -> {
                when (next) {
                    AdaptiveInspectionEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection estimate $code cannot move from $status to $next")
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

    fun weightedValue(): Double = round((floorValue - windowDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveInspectionEstimate {
        return AdaptiveInspectionEstimate(
            code = code,
            title = title,
            region = region,
            threshold = threshold,
            margin = margin,
            attempts = attempts,
            floorValue = floorValue,
            attributes = attributes,
            currency = currency,
            category = category,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive inspection estimate in status new. */
fun newAdaptiveInspectionEstimate(code: String, title: String): AdaptiveInspectionEstimate {
    return AdaptiveInspectionEstimate(
        code = code,
        title = title,
        region = "",
        threshold = 0.0,
        margin = 0.0,
        attempts = 0,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        currency = "",
        category = "",
        windowDays = 0,
        status = AdaptiveInspectionEstimateStatus.NEW,
    )
}
