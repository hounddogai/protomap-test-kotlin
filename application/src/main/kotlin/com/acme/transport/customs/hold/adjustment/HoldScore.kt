package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.text.normalize

/** Where a hold score is in its life cycle. */
enum class HoldScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One hold score, with what the planners need to act on it. */
data class HoldScore(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var routeCode: String,
    var attempts: Int,
    var status: HoldScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldScoreStatus): Boolean {
        when (status) {
            HoldScoreStatus.PLANNED -> return next == HoldScoreStatus.SCHEDULED || next == HoldScoreStatus.COMPLETED
            HoldScoreStatus.SCHEDULED -> return next == HoldScoreStatus.COMPLETED || next == HoldScoreStatus.CANCELLED
            HoldScoreStatus.IN_PROGRESS -> return next == HoldScoreStatus.COMPLETED || next == HoldScoreStatus.CANCELLED
            HoldScoreStatus.COMPLETED -> return next == HoldScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HoldScoreStatus) {
        if (!canMoveTo(next)) {
            error("hold score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): HoldScore {
        return HoldScore(
            code = code,
            name = name,
            pinned = pinned,
            routeCode = routeCode,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold score in status planned. */
fun newHoldScore(code: String, name: String): HoldScore {
    return HoldScore(
        code = code,
        name = name,
        pinned = false,
        routeCode = "",
        attempts = 0,
        status = HoldScoreStatus.PLANNED,
    )
}
