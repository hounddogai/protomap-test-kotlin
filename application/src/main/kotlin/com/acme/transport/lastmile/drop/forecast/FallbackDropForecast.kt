package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback drop forecast records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a fallback drop forecast is in its life cycle. */
enum class FallbackDropForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fallback drop forecast and its current status. */
data class FallbackDropForecast(
    var code: String,
    var title: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var reviewer: String,
    var windowDays: Int,
    var shiftCode: String,
    var description: String,
    var expiresAt: Int,
    var timezone: String,
    var etaMinutes: Int,
    var status: FallbackDropForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDropForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDropForecastStatus): Boolean {
        when (status) {
            FallbackDropForecastStatus.NEW -> return next == FallbackDropForecastStatus.QUEUED || next == FallbackDropForecastStatus.DONE
            FallbackDropForecastStatus.QUEUED -> {
                when (next) {
                    FallbackDropForecastStatus.RUNNING, FallbackDropForecastStatus.FAILED -> return true
                }
                return false
            }
            FallbackDropForecastStatus.RUNNING -> {
                when (next) {
                    FallbackDropForecastStatus.DONE, FallbackDropForecastStatus.FAILED -> return true
                }
                return false
            }
            FallbackDropForecastStatus.DONE -> return next == FallbackDropForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackDropForecastStatus) {
        if (!canMoveTo(next)) {
            error("fallback drop forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((etaMinutes.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackDropForecast {
        return FallbackDropForecast(
            code = code,
            title = title,
            sequence = sequence,
            tags = tags,
            reviewer = reviewer,
            windowDays = windowDays,
            shiftCode = shiftCode,
            description = description,
            expiresAt = expiresAt,
            timezone = timezone,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback drop forecast in status new. */
fun newFallbackDropForecast(code: String, title: String): FallbackDropForecast {
    return FallbackDropForecast(
        code = code,
        title = title,
        sequence = 0,
        tags = mutableListOf<String>(),
        reviewer = "",
        windowDays = 0,
        shiftCode = "",
        description = "",
        expiresAt = 0,
        timezone = "",
        etaMinutes = 0,
        status = FallbackDropForecastStatus.NEW,
    )
}
