package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer yard schedule is in its life cycle. */
enum class SeasonalTrailerYardScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a seasonal trailer yard schedule and its current status. */
data class SeasonalTrailerYardSchedule(
    var code: String,
    var name: String,
    var startsAt: Int,
    var category: String,
    var region: String,
    var currency: String,
    var notes: String,
    var ownerId: String,
    var graceDays: Int,
    var loadKg: Double,
    var status: SeasonalTrailerYardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardScheduleStatus): Boolean {
        when (status) {
            SeasonalTrailerYardScheduleStatus.PROPOSED -> {
                when (next) {
                    SeasonalTrailerYardScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalTrailerYardScheduleStatus.APPROVED -> {
                when (next) {
                    SeasonalTrailerYardScheduleStatus.REJECTED, SeasonalTrailerYardScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalTrailerYardScheduleStatus.REJECTED -> return next == SeasonalTrailerYardScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round((graceDays.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTrailerYardSchedule {
        return SeasonalTrailerYardSchedule(
            code = code,
            name = name,
            startsAt = startsAt,
            category = category,
            region = region,
            currency = currency,
            notes = notes,
            ownerId = ownerId,
            graceDays = graceDays,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal trailer yard schedule in status proposed. */
fun newSeasonalTrailerYardSchedule(code: String, name: String): SeasonalTrailerYardSchedule {
    return SeasonalTrailerYardSchedule(
        code = code,
        name = name,
        startsAt = 0,
        category = "",
        region = "",
        currency = "",
        notes = "",
        ownerId = "",
        graceDays = 0,
        loadKg = 0.0,
        status = SeasonalTrailerYardScheduleStatus.PROPOSED,
    )
}
