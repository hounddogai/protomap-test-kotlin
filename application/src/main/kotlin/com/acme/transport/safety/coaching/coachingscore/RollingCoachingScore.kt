package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling coaching score records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a rolling coaching score is in its life cycle. */
enum class RollingCoachingScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rolling coaching score and its current status. */
data class RollingCoachingScore(
    var code: String,
    var name: String,
    var stopCount: Int,
    var ratio: Double,
    var graceDays: Int,
    var etaMinutes: Int,
    var checksum: String,
    var status: RollingCoachingScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCoachingScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCoachingScoreStatus): Boolean {
        when (status) {
            RollingCoachingScoreStatus.NEW -> {
                when (next) {
                    RollingCoachingScoreStatus.FAILED -> return true
                }
                return false
            }
            RollingCoachingScoreStatus.QUEUED -> {
                when (next) {
                    RollingCoachingScoreStatus.DONE, RollingCoachingScoreStatus.FAILED -> return true
                }
                return false
            }
            RollingCoachingScoreStatus.RUNNING -> {
                when (next) {
                    RollingCoachingScoreStatus.DONE -> return true
                }
                return false
            }
            RollingCoachingScoreStatus.DONE -> return next == RollingCoachingScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingCoachingScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling coaching score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((stopCount.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RollingCoachingScore {
        return RollingCoachingScore(
            code = code,
            name = name,
            stopCount = stopCount,
            ratio = ratio,
            graceDays = graceDays,
            etaMinutes = etaMinutes,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling coaching score in status new. */
fun newRollingCoachingScore(code: String, name: String): RollingCoachingScore {
    return RollingCoachingScore(
        code = code,
        name = name,
        stopCount = 0,
        ratio = 0.0,
        graceDays = 0,
        etaMinutes = 0,
        checksum = "",
        status = RollingCoachingScoreStatus.NEW,
    )
}
