package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive roster score records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a adaptive roster score is in its life cycle. */
enum class AdaptiveRosterScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive roster score as the roster team keeps it. */
data class AdaptiveRosterScore(
    var code: String,
    var title: String,
    var source: String,
    var floorValue: Double,
    var minimum: Double,
    var batchSize: Int,
    var status: AdaptiveRosterScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRosterScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRosterScoreStatus): Boolean {
        when (status) {
            AdaptiveRosterScoreStatus.NEW -> {
                when (next) {
                    AdaptiveRosterScoreStatus.QUEUED, AdaptiveRosterScoreStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveRosterScoreStatus.QUEUED -> return next == AdaptiveRosterScoreStatus.RUNNING
            AdaptiveRosterScoreStatus.RUNNING -> return next == AdaptiveRosterScoreStatus.DONE || next == AdaptiveRosterScoreStatus.FAILED
            AdaptiveRosterScoreStatus.DONE -> {
                when (next) {
                    AdaptiveRosterScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRosterScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive roster score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round(floorValue * batchSize.toDouble() * 100) / 100

    fun snapshot(): AdaptiveRosterScore {
        return AdaptiveRosterScore(
            code = code,
            title = title,
            source = source,
            floorValue = floorValue,
            minimum = minimum,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive roster score in status new. */
fun newAdaptiveRosterScore(code: String, title: String): AdaptiveRosterScore {
    return AdaptiveRosterScore(
        code = code,
        title = title,
        source = "",
        floorValue = 0.0,
        minimum = 0.0,
        batchSize = 0,
        status = AdaptiveRosterScoreStatus.NEW,
    )
}
