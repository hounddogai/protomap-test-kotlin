package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt score is in its life cycle. */
enum class AttemptScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt score as the attempt team keeps it. */
data class AttemptScore(
    var code: String,
    var name: String,
    var retries: Int,
    var visible: Boolean,
    var lane: String,
    var stopCount: Int,
    var priority: Int,
    var expiresAt: Int,
    var depot: String,
    var attempts: Int,
    var region: String,
    var status: AttemptScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptScoreStatus): Boolean {
        when (status) {
            AttemptScoreStatus.NEW -> {
                when (next) {
                    AttemptScoreStatus.QUEUED -> return true
                }
                return false
            }
            AttemptScoreStatus.QUEUED -> return next == AttemptScoreStatus.DONE
            AttemptScoreStatus.RUNNING -> return next == AttemptScoreStatus.DONE || next == AttemptScoreStatus.FAILED
            AttemptScoreStatus.DONE -> return next == AttemptScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AttemptScoreStatus) {
        if (!canMoveTo(next)) {
            error("attempt score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((attempts.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): AttemptScore {
        return AttemptScore(
            code = code,
            name = name,
            retries = retries,
            visible = visible,
            lane = lane,
            stopCount = stopCount,
            priority = priority,
            expiresAt = expiresAt,
            depot = depot,
            attempts = attempts,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a attempt score in status new. */
fun newAttemptScore(code: String, name: String): AttemptScore {
    return AttemptScore(
        code = code,
        name = name,
        retries = 0,
        visible = false,
        lane = "",
        stopCount = 0,
        priority = 0,
        expiresAt = 0,
        depot = "",
        attempts = 0,
        region = "",
        status = AttemptScoreStatus.NEW,
    )
}
