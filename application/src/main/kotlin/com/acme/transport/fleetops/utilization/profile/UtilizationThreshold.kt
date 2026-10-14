package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization threshold is in its life cycle. */
enum class UtilizationThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One utilization threshold, with what the planners need to act on it. */
data class UtilizationThreshold(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var threshold: Double,
    var locale: String,
    var amount: Double,
    var discount: Double,
    var odometer: Int,
    var category: String,
    var startsAt: Int,
    var status: UtilizationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationThresholdStatus): Boolean {
        when (status) {
            UtilizationThresholdStatus.PROPOSED -> return next == UtilizationThresholdStatus.WITHDRAWN
            UtilizationThresholdStatus.APPROVED -> return next == UtilizationThresholdStatus.WITHDRAWN
            UtilizationThresholdStatus.REJECTED -> return next == UtilizationThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: UtilizationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("utilization threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((threshold + startsAt.toDouble()) * 100) / 100

    fun snapshot(): UtilizationThreshold {
        return UtilizationThreshold(
            code = code,
            name = name,
            tags = tags,
            distanceKm = distanceKm,
            threshold = threshold,
            locale = locale,
            amount = amount,
            discount = discount,
            odometer = odometer,
            category = category,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a utilization threshold in status proposed. */
fun newUtilizationThreshold(code: String, name: String): UtilizationThreshold {
    return UtilizationThreshold(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        threshold = 0.0,
        locale = "",
        amount = 0.0,
        discount = 0.0,
        odometer = 0,
        category = "",
        startsAt = 0,
        status = UtilizationThresholdStatus.PROPOSED,
    )
}
