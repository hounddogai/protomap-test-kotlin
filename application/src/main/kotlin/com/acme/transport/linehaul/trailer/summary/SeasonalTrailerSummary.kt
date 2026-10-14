package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer summary is in its life cycle. */
enum class SeasonalTrailerSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal trailer summary and its current status. */
data class SeasonalTrailerSummary(
    var code: String,
    var title: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var fuelLiters: Double,
    var expiresAt: Int,
    var name: String,
    var startsAt: Int,
    var durationMinutes: Int,
    var status: SeasonalTrailerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerSummaryStatus): Boolean {
        when (status) {
            SeasonalTrailerSummaryStatus.CREATED -> return next == SeasonalTrailerSummaryStatus.RETIRED
            SeasonalTrailerSummaryStatus.VALIDATED -> return next == SeasonalTrailerSummaryStatus.RETIRED
            SeasonalTrailerSummaryStatus.PUBLISHED -> return next == SeasonalTrailerSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((startsAt.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTrailerSummary {
        return SeasonalTrailerSummary(
            code = code,
            title = title,
            vehicleId = vehicleId,
            attributes = attributes,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            expiresAt = expiresAt,
            name = name,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal trailer summary in status created. */
fun newSeasonalTrailerSummary(code: String, title: String): SeasonalTrailerSummary {
    return SeasonalTrailerSummary(
        code = code,
        title = title,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        fuelLiters = 0.0,
        expiresAt = 0,
        name = "",
        startsAt = 0,
        durationMinutes = 0,
        status = SeasonalTrailerSummaryStatus.CREATED,
    )
}
