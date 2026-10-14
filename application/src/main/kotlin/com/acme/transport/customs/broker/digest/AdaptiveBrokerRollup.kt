package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive broker rollup is in its life cycle. */
enum class AdaptiveBrokerRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive broker rollup, with what the planners need to act on it. */
data class AdaptiveBrokerRollup(
    var code: String,
    var name: String,
    var sequence: Int,
    var startsAt: Int,
    var reviewer: String,
    var maximum: Double,
    var createdAt: Int,
    var notes: String,
    var status: AdaptiveBrokerRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBrokerRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBrokerRollupStatus): Boolean {
        when (status) {
            AdaptiveBrokerRollupStatus.DRAFT -> {
                when (next) {
                    AdaptiveBrokerRollupStatus.ACTIVE, AdaptiveBrokerRollupStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveBrokerRollupStatus.PENDING -> return next == AdaptiveBrokerRollupStatus.ACTIVE || next == AdaptiveBrokerRollupStatus.SUSPENDED
            AdaptiveBrokerRollupStatus.ACTIVE -> return next == AdaptiveBrokerRollupStatus.SUSPENDED || next == AdaptiveBrokerRollupStatus.CLOSED
            AdaptiveBrokerRollupStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveBrokerRollupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBrokerRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive broker rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((startsAt.toDouble() + maximum) * 100) / 100

    fun snapshot(): AdaptiveBrokerRollup {
        return AdaptiveBrokerRollup(
            code = code,
            name = name,
            sequence = sequence,
            startsAt = startsAt,
            reviewer = reviewer,
            maximum = maximum,
            createdAt = createdAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive broker rollup in status draft. */
fun newAdaptiveBrokerRollup(code: String, name: String): AdaptiveBrokerRollup {
    return AdaptiveBrokerRollup(
        code = code,
        name = name,
        sequence = 0,
        startsAt = 0,
        reviewer = "",
        maximum = 0.0,
        createdAt = 0,
        notes = "",
        status = AdaptiveBrokerRollupStatus.DRAFT,
    )
}
