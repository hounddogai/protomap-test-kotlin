package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal attempt entry is in its life cycle. */
enum class SeasonalAttemptEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal attempt entry and its current status. */
data class SeasonalAttemptEntry(
    var code: String,
    var title: String,
    var country: String,
    var description: String,
    var distanceKm: Double,
    var batchSize: Int,
    var fuelLiters: Double,
    var quantity: Int,
    var score: Double,
    var shiftCode: String,
    var status: SeasonalAttemptEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAttemptEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAttemptEntryStatus): Boolean {
        when (status) {
            SeasonalAttemptEntryStatus.CREATED -> {
                when (next) {
                    SeasonalAttemptEntryStatus.VALIDATED, SeasonalAttemptEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalAttemptEntryStatus.VALIDATED -> {
                when (next) {
                    SeasonalAttemptEntryStatus.PUBLISHED, SeasonalAttemptEntryStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalAttemptEntryStatus.PUBLISHED -> {
                when (next) {
                    SeasonalAttemptEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAttemptEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal attempt entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal attempt entry"} $code"

    fun weightedVolume(): Double = round((score - fuelLiters) * 100) / 100

    fun snapshot(): SeasonalAttemptEntry {
        return SeasonalAttemptEntry(
            code = code,
            title = title,
            country = country,
            description = description,
            distanceKm = distanceKm,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            quantity = quantity,
            score = score,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal attempt entry in status created. */
fun newSeasonalAttemptEntry(code: String, title: String): SeasonalAttemptEntry {
    return SeasonalAttemptEntry(
        code = code,
        title = title,
        country = "",
        description = "",
        distanceKm = 0.0,
        batchSize = 0,
        fuelLiters = 0.0,
        quantity = 0,
        score = 0.0,
        shiftCode = "",
        status = SeasonalAttemptEntryStatus.CREATED,
    )
}
