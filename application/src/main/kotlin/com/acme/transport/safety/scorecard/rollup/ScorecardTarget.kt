package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard target is in its life cycle. */
enum class ScorecardTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scorecard target, with what the planners need to act on it. */
data class ScorecardTarget(
    var code: String,
    var name: String,
    var phone: String,
    var pinned: Boolean,
    var weight: Double,
    var description: String,
    var expiresAt: Int,
    var status: ScorecardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardTargetStatus): Boolean {
        when (status) {
            ScorecardTargetStatus.NEW -> {
                when (next) {
                    ScorecardTargetStatus.RUNNING -> return true
                }
                return false
            }
            ScorecardTargetStatus.QUEUED -> return next == ScorecardTargetStatus.RUNNING
            ScorecardTargetStatus.RUNNING -> {
                when (next) {
                    ScorecardTargetStatus.FAILED -> return true
                }
                return false
            }
            ScorecardTargetStatus.DONE -> return next == ScorecardTargetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScorecardTargetStatus) {
        if (!canMoveTo(next)) {
            error("scorecard target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scorecard target"} $code"

    fun weightedVolume(): Double = round((weight + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScorecardTarget {
        return ScorecardTarget(
            code = code,
            name = name,
            phone = phone,
            pinned = pinned,
            weight = weight,
            description = description,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scorecard target in status new. */
fun newScorecardTarget(code: String, name: String): ScorecardTarget {
    return ScorecardTarget(
        code = code,
        name = name,
        phone = "",
        pinned = false,
        weight = 0.0,
        description = "",
        expiresAt = 0,
        status = ScorecardTargetStatus.NEW,
    )
}
