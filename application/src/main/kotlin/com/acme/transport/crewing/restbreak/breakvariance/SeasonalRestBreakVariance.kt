package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal rest break variance records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a seasonal rest break variance is in its life cycle. */
enum class SeasonalRestBreakVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal rest break variance as the rest break team keeps it. */
data class SeasonalRestBreakVariance(
    var code: String,
    var title: String,
    var floorValue: Double,
    var revision: Int,
    var driverId: String,
    var weight: Double,
    var attributes: MutableMap<String, String>,
    var priority: Int,
    var status: SeasonalRestBreakVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRestBreakVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRestBreakVarianceStatus): Boolean {
        when (status) {
            SeasonalRestBreakVarianceStatus.CREATED -> return next == SeasonalRestBreakVarianceStatus.PUBLISHED || next == SeasonalRestBreakVarianceStatus.RETIRED
            SeasonalRestBreakVarianceStatus.VALIDATED -> return next == SeasonalRestBreakVarianceStatus.PUBLISHED || next == SeasonalRestBreakVarianceStatus.RETIRED
            SeasonalRestBreakVarianceStatus.PUBLISHED -> return next == SeasonalRestBreakVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRestBreakVarianceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal rest break variance $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(weight * priority.toDouble() * 100) / 100

    fun snapshot(): SeasonalRestBreakVariance {
        return SeasonalRestBreakVariance(
            code = code,
            title = title,
            floorValue = floorValue,
            revision = revision,
            driverId = driverId,
            weight = weight,
            attributes = attributes,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal rest break variance in status created. */
fun newSeasonalRestBreakVariance(code: String, title: String): SeasonalRestBreakVariance {
    return SeasonalRestBreakVariance(
        code = code,
        title = title,
        floorValue = 0.0,
        revision = 0,
        driverId = "",
        weight = 0.0,
        attributes = mutableMapOf<String, String>(),
        priority = 0,
        status = SeasonalRestBreakVarianceStatus.CREATED,
    )
}
