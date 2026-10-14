package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard score records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard score is in its life cycle. */
enum class TrailerYardScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a trailer yard score and its current status. */
data class TrailerYardScore(
    var code: String,
    var title: String,
    var ratio: Double,
    var maximum: Double,
    var phone: String,
    var floorValue: Double,
    var shiftCode: String,
    var attempts: Int,
    var status: TrailerYardScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardScoreStatus): Boolean {
        when (status) {
            TrailerYardScoreStatus.REQUESTED -> {
                when (next) {
                    TrailerYardScoreStatus.REVOKED, TrailerYardScoreStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardScoreStatus.GRANTED -> {
                when (next) {
                    TrailerYardScoreStatus.REVOKED, TrailerYardScoreStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardScoreStatus.REVOKED -> return next == TrailerYardScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardScoreStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round((ratio + attempts.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardScore {
        return TrailerYardScore(
            code = code,
            title = title,
            ratio = ratio,
            maximum = maximum,
            phone = phone,
            floorValue = floorValue,
            shiftCode = shiftCode,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard score in status requested. */
fun newTrailerYardScore(code: String, title: String): TrailerYardScore {
    return TrailerYardScore(
        code = code,
        title = title,
        ratio = 0.0,
        maximum = 0.0,
        phone = "",
        floorValue = 0.0,
        shiftCode = "",
        attempts = 0,
        status = TrailerYardScoreStatus.REQUESTED,
    )
}
