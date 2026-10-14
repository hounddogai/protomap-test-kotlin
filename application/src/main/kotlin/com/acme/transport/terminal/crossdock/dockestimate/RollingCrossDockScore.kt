package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.text.normalize

/** Where a rolling cross dock score is in its life cycle. */
enum class RollingCrossDockScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling cross dock score and its current status. */
data class RollingCrossDockScore(
    var code: String,
    var title: String,
    var score: Double,
    var currency: String,
    var contactEmail: String,
    var status: RollingCrossDockScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCrossDockScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCrossDockScoreStatus): Boolean {
        when (status) {
            RollingCrossDockScoreStatus.PLANNED -> return next == RollingCrossDockScoreStatus.SCHEDULED
            RollingCrossDockScoreStatus.SCHEDULED -> {
                when (next) {
                    RollingCrossDockScoreStatus.COMPLETED, RollingCrossDockScoreStatus.CANCELLED -> return true
                }
                return false
            }
            RollingCrossDockScoreStatus.IN_PROGRESS -> return next == RollingCrossDockScoreStatus.COMPLETED || next == RollingCrossDockScoreStatus.CANCELLED
            RollingCrossDockScoreStatus.COMPLETED -> return next == RollingCrossDockScoreStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingCrossDockScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling cross dock score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = score

    fun snapshot(): RollingCrossDockScore {
        return RollingCrossDockScore(
            code = code,
            title = title,
            score = score,
            currency = currency,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling cross dock score in status planned. */
fun newRollingCrossDockScore(code: String, title: String): RollingCrossDockScore {
    return RollingCrossDockScore(
        code = code,
        title = title,
        score = 0.0,
        currency = "",
        contactEmail = "",
        status = RollingCrossDockScoreStatus.PLANNED,
    )
}
