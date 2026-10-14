package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling broker score is in its life cycle. */
enum class RollingBrokerScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling broker score and its current status. */
data class RollingBrokerScore(
    var code: String,
    var title: String,
    var rate: Double,
    var sequence: Int,
    var revision: Int,
    var windowDays: Int,
    var active: Boolean,
    var amount: Double,
    var status: RollingBrokerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBrokerScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBrokerScoreStatus): Boolean {
        when (status) {
            RollingBrokerScoreStatus.DRAFT -> {
                when (next) {
                    RollingBrokerScoreStatus.CLOSED -> return true
                }
                return false
            }
            RollingBrokerScoreStatus.PENDING -> {
                when (next) {
                    RollingBrokerScoreStatus.ACTIVE, RollingBrokerScoreStatus.CLOSED -> return true
                }
                return false
            }
            RollingBrokerScoreStatus.ACTIVE -> return next == RollingBrokerScoreStatus.CLOSED
            RollingBrokerScoreStatus.SUSPENDED -> return next == RollingBrokerScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingBrokerScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling broker score $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"rolling broker score"} $code"

    fun weightedExposure(): Double = round((rate + sequence.toDouble()) * 100) / 100

    fun snapshot(): RollingBrokerScore {
        return RollingBrokerScore(
            code = code,
            title = title,
            rate = rate,
            sequence = sequence,
            revision = revision,
            windowDays = windowDays,
            active = active,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling broker score in status draft. */
fun newRollingBrokerScore(code: String, title: String): RollingBrokerScore {
    return RollingBrokerScore(
        code = code,
        title = title,
        rate = 0.0,
        sequence = 0,
        revision = 0,
        windowDays = 0,
        active = false,
        amount = 0.0,
        status = RollingBrokerScoreStatus.DRAFT,
    )
}
