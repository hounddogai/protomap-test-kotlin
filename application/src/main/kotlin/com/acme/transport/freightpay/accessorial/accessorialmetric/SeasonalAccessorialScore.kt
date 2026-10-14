package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal accessorial score is in its life cycle. */
enum class SeasonalAccessorialScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal accessorial score as the accessorial team keeps it. */
data class SeasonalAccessorialScore(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var label: String,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var floorValue: Double,
    var summary: String,
    var status: SeasonalAccessorialScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAccessorialScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAccessorialScoreStatus): Boolean {
        when (status) {
            SeasonalAccessorialScoreStatus.PLANNED -> return next == SeasonalAccessorialScoreStatus.SCHEDULED || next == SeasonalAccessorialScoreStatus.CANCELLED
            SeasonalAccessorialScoreStatus.SCHEDULED -> {
                when (next) {
                    SeasonalAccessorialScoreStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SeasonalAccessorialScoreStatus.IN_PROGRESS -> return next == SeasonalAccessorialScoreStatus.CANCELLED
            SeasonalAccessorialScoreStatus.COMPLETED -> return next == SeasonalAccessorialScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalAccessorialScoreStatus) {
        if (!canMoveTo(next)) {
            error("seasonal accessorial score $code cannot move from $status to $next")
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

    fun projectedTotal(): Double = round(floorValue * distanceKm * 100) / 100

    fun snapshot(): SeasonalAccessorialScore {
        return SeasonalAccessorialScore(
            code = code,
            name = name,
            distanceKm = distanceKm,
            label = label,
            locale = locale,
            attributes = attributes,
            ownerId = ownerId,
            floorValue = floorValue,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal accessorial score in status planned. */
fun newSeasonalAccessorialScore(code: String, name: String): SeasonalAccessorialScore {
    return SeasonalAccessorialScore(
        code = code,
        name = name,
        distanceKm = 0.0,
        label = "",
        locale = "",
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        floorValue = 0.0,
        summary = "",
        status = SeasonalAccessorialScoreStatus.PLANNED,
    )
}
