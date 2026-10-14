package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal trailer yard score is in its life cycle. */
enum class SeasonalTrailerYardScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal trailer yard score and its current status. */
data class SeasonalTrailerYardScore(
    var code: String,
    var title: String,
    var startsAt: Int,
    var driverId: String,
    var category: String,
    var attempts: Int,
    var checksum: String,
    var trailerId: String,
    var contactEmail: String,
    var segment: String,
    var cost: Double,
    var status: SeasonalTrailerYardScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTrailerYardScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTrailerYardScoreStatus): Boolean {
        when (status) {
            SeasonalTrailerYardScoreStatus.CREATED -> {
                when (next) {
                    SeasonalTrailerYardScoreStatus.VALIDATED, SeasonalTrailerYardScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalTrailerYardScoreStatus.VALIDATED -> {
                when (next) {
                    SeasonalTrailerYardScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalTrailerYardScoreStatus.PUBLISHED -> return next == SeasonalTrailerYardScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalTrailerYardScoreStatus) {
        if (!canMoveTo(next)) {
            error("seasonal trailer yard score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal trailer yard score"} $code"

    fun projectedVolume(): Double = round((startsAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTrailerYardScore {
        return SeasonalTrailerYardScore(
            code = code,
            title = title,
            startsAt = startsAt,
            driverId = driverId,
            category = category,
            attempts = attempts,
            checksum = checksum,
            trailerId = trailerId,
            contactEmail = contactEmail,
            segment = segment,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal trailer yard score in status created. */
fun newSeasonalTrailerYardScore(code: String, title: String): SeasonalTrailerYardScore {
    return SeasonalTrailerYardScore(
        code = code,
        title = title,
        startsAt = 0,
        driverId = "",
        category = "",
        attempts = 0,
        checksum = "",
        trailerId = "",
        contactEmail = "",
        segment = "",
        cost = 0.0,
        status = SeasonalTrailerYardScoreStatus.CREATED,
    )
}
