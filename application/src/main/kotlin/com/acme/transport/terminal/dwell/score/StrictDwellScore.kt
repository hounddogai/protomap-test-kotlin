package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict dwell score records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a strict dwell score is in its life cycle. */
enum class StrictDwellScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict dwell score and its current status. */
data class StrictDwellScore(
    var code: String,
    var name: String,
    var sequence: Int,
    var country: String,
    var loadKg: Double,
    var status: StrictDwellScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDwellScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDwellScoreStatus): Boolean {
        when (status) {
            StrictDwellScoreStatus.NEW -> return next == StrictDwellScoreStatus.RUNNING
            StrictDwellScoreStatus.QUEUED -> return next == StrictDwellScoreStatus.RUNNING
            StrictDwellScoreStatus.RUNNING -> {
                when (next) {
                    StrictDwellScoreStatus.DONE -> return true
                }
                return false
            }
            StrictDwellScoreStatus.DONE -> {
                when (next) {
                    StrictDwellScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDwellScoreStatus) {
        if (!canMoveTo(next)) {
            error("strict dwell score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict dwell score"} $code"

    fun effectiveLoad(): Double = round((loadKg - sequence.toDouble()) * 100) / 100

    fun snapshot(): StrictDwellScore {
        return StrictDwellScore(
            code = code,
            name = name,
            sequence = sequence,
            country = country,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict dwell score in status new. */
fun newStrictDwellScore(code: String, name: String): StrictDwellScore {
    return StrictDwellScore(
        code = code,
        name = name,
        sequence = 0,
        country = "",
        loadKg = 0.0,
        status = StrictDwellScoreStatus.NEW,
    )
}
