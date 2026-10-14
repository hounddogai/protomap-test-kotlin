package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy trailer variance is in its life cycle. */
enum class LegacyTrailerVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy trailer variance as the trailer team keeps it. */
data class LegacyTrailerVariance(
    var code: String,
    var label: String,
    var windowDays: Int,
    var fuelLiters: Double,
    var distanceKm: Double,
    var quantity: Int,
    var title: String,
    var budget: Double,
    var archived: Boolean,
    var status: LegacyTrailerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerVarianceStatus): Boolean {
        when (status) {
            LegacyTrailerVarianceStatus.PLANNED -> return next == LegacyTrailerVarianceStatus.COMPLETED
            LegacyTrailerVarianceStatus.SCHEDULED -> {
                when (next) {
                    LegacyTrailerVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyTrailerVarianceStatus.IN_PROGRESS -> return next == LegacyTrailerVarianceStatus.COMPLETED
            LegacyTrailerVarianceStatus.COMPLETED -> return next == LegacyTrailerVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyTrailerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy trailer variance"} $code"

    fun effectiveVolume(): Double = round((distanceKm - fuelLiters) * 100) / 100

    fun snapshot(): LegacyTrailerVariance {
        return LegacyTrailerVariance(
            code = code,
            label = label,
            windowDays = windowDays,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            quantity = quantity,
            title = title,
            budget = budget,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy trailer variance in status planned. */
fun newLegacyTrailerVariance(code: String, label: String): LegacyTrailerVariance {
    return LegacyTrailerVariance(
        code = code,
        label = label,
        windowDays = 0,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        quantity = 0,
        title = "",
        budget = 0.0,
        archived = false,
        status = LegacyTrailerVarianceStatus.PLANNED,
    )
}
