package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient coaching forecast is in its life cycle. */
enum class LenientCoachingForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient coaching forecast. Its code is unique within a tenant. */
data class LenientCoachingForecast(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var threshold: Double,
    var approved: Boolean,
    var createdAt: Int,
    var externalRef: String,
    var archived: Boolean,
    var startsAt: Int,
    var status: LenientCoachingForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCoachingForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCoachingForecastStatus): Boolean {
        when (status) {
            LenientCoachingForecastStatus.CREATED -> {
                when (next) {
                    LenientCoachingForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientCoachingForecastStatus.VALIDATED -> return next == LenientCoachingForecastStatus.PUBLISHED || next == LenientCoachingForecastStatus.RETIRED
            LenientCoachingForecastStatus.PUBLISHED -> return next == LenientCoachingForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientCoachingForecastStatus) {
        if (!canMoveTo(next)) {
            error("lenient coaching forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round((threshold - startsAt.toDouble()) * 100) / 100

    fun snapshot(): LenientCoachingForecast {
        return LenientCoachingForecast(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            threshold = threshold,
            approved = approved,
            createdAt = createdAt,
            externalRef = externalRef,
            archived = archived,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient coaching forecast in status created. */
fun newLenientCoachingForecast(code: String, title: String): LenientCoachingForecast {
    return LenientCoachingForecast(
        code = code,
        title = title,
        durationMinutes = 0,
        threshold = 0.0,
        approved = false,
        createdAt = 0,
        externalRef = "",
        archived = false,
        startsAt = 0,
        status = LenientCoachingForecastStatus.CREATED,
    )
}
