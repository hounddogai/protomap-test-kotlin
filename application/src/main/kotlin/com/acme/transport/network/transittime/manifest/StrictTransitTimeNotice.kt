package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict transit time notice is in its life cycle. */
enum class StrictTransitTimeNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict transit time notice as the transit time team keeps it. */
data class StrictTransitTimeNotice(
    var code: String,
    var title: String,
    var stopCount: Int,
    var country: String,
    var maximum: Double,
    var margin: Double,
    var shiftCode: String,
    var budget: Double,
    var attempts: Int,
    var startsAt: Int,
    var fuelLiters: Double,
    var status: StrictTransitTimeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTransitTimeNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTransitTimeNoticeStatus): Boolean {
        when (status) {
            StrictTransitTimeNoticeStatus.PLANNED -> return next == StrictTransitTimeNoticeStatus.IN_PROGRESS
            StrictTransitTimeNoticeStatus.SCHEDULED -> return next == StrictTransitTimeNoticeStatus.COMPLETED
            StrictTransitTimeNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    StrictTransitTimeNoticeStatus.COMPLETED, StrictTransitTimeNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            StrictTransitTimeNoticeStatus.COMPLETED -> {
                when (next) {
                    StrictTransitTimeNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTransitTimeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("strict transit time notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((attempts.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictTransitTimeNotice {
        return StrictTransitTimeNotice(
            code = code,
            title = title,
            stopCount = stopCount,
            country = country,
            maximum = maximum,
            margin = margin,
            shiftCode = shiftCode,
            budget = budget,
            attempts = attempts,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict transit time notice in status planned. */
fun newStrictTransitTimeNotice(code: String, title: String): StrictTransitTimeNotice {
    return StrictTransitTimeNotice(
        code = code,
        title = title,
        stopCount = 0,
        country = "",
        maximum = 0.0,
        margin = 0.0,
        shiftCode = "",
        budget = 0.0,
        attempts = 0,
        startsAt = 0,
        fuelLiters = 0.0,
        status = StrictTransitTimeNoticeStatus.PLANNED,
    )
}
