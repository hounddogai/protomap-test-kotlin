package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop forecast is in its life cycle. */
enum class DropForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a drop forecast and its current status. */
data class DropForecast(
    var code: String,
    var title: String,
    var country: String,
    var updatedAt: Int,
    var notes: String,
    var version: Int,
    var startsAt: Int,
    var score: Double,
    var threshold: Double,
    var currency: String,
    var summary: String,
    var status: DropForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropForecastStatus): Boolean {
        when (status) {
            DropForecastStatus.IDLE -> return next == DropForecastStatus.WARMING || next == DropForecastStatus.DRAINING
            DropForecastStatus.WARMING -> {
                when (next) {
                    DropForecastStatus.STOPPED -> return true
                }
                return false
            }
            DropForecastStatus.READY -> {
                when (next) {
                    DropForecastStatus.DRAINING, DropForecastStatus.STOPPED -> return true
                }
                return false
            }
            DropForecastStatus.DRAINING -> {
                when (next) {
                    DropForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropForecastStatus) {
        if (!canMoveTo(next)) {
            error("drop forecast $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round((startsAt.toDouble() - score) * 100) / 100

    fun snapshot(): DropForecast {
        return DropForecast(
            code = code,
            title = title,
            country = country,
            updatedAt = updatedAt,
            notes = notes,
            version = version,
            startsAt = startsAt,
            score = score,
            threshold = threshold,
            currency = currency,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a drop forecast in status idle. */
fun newDropForecast(code: String, title: String): DropForecast {
    return DropForecast(
        code = code,
        title = title,
        country = "",
        updatedAt = 0,
        notes = "",
        version = 0,
        startsAt = 0,
        score = 0.0,
        threshold = 0.0,
        currency = "",
        summary = "",
        status = DropForecastStatus.IDLE,
    )
}
