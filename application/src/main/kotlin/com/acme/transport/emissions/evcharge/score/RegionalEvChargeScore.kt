package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional ev charge score records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a regional ev charge score is in its life cycle. */
enum class RegionalEvChargeScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional ev charge score, with what the planners need to act on it. */
data class RegionalEvChargeScore(
    var code: String,
    var name: String,
    var windowDays: Int,
    var revision: Int,
    var createdAt: Int,
    var status: RegionalEvChargeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEvChargeScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEvChargeScoreStatus): Boolean {
        when (status) {
            RegionalEvChargeScoreStatus.OPEN -> return next == RegionalEvChargeScoreStatus.REOPENED
            RegionalEvChargeScoreStatus.IN_REVIEW -> {
                when (next) {
                    RegionalEvChargeScoreStatus.RESOLVED, RegionalEvChargeScoreStatus.REOPENED -> return true
                }
                return false
            }
            RegionalEvChargeScoreStatus.RESOLVED -> {
                when (next) {
                    RegionalEvChargeScoreStatus.REOPENED, RegionalEvChargeScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalEvChargeScoreStatus.REOPENED -> {
                when (next) {
                    RegionalEvChargeScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalEvChargeScoreStatus) {
        if (!canMoveTo(next)) {
            error("regional ev charge score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun weightedExposure(): Double = round((revision.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalEvChargeScore {
        return RegionalEvChargeScore(
            code = code,
            name = name,
            windowDays = windowDays,
            revision = revision,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional ev charge score in status open. */
fun newRegionalEvChargeScore(code: String, name: String): RegionalEvChargeScore {
    return RegionalEvChargeScore(
        code = code,
        name = name,
        windowDays = 0,
        revision = 0,
        createdAt = 0,
        status = RegionalEvChargeScoreStatus.OPEN,
    )
}
