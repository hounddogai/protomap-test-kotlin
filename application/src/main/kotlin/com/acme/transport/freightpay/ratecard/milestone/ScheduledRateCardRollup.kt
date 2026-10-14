package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled rate card rollup is in its life cycle. */
enum class ScheduledRateCardRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled rate card rollup and its current status. */
data class ScheduledRateCardRollup(
    var code: String,
    var name: String,
    var rate: Double,
    var region: String,
    var createdAt: Int,
    var status: ScheduledRateCardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRateCardRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRateCardRollupStatus): Boolean {
        when (status) {
            ScheduledRateCardRollupStatus.PLANNED -> return next == ScheduledRateCardRollupStatus.SCHEDULED || next == ScheduledRateCardRollupStatus.COMPLETED
            ScheduledRateCardRollupStatus.SCHEDULED -> {
                when (next) {
                    ScheduledRateCardRollupStatus.IN_PROGRESS, ScheduledRateCardRollupStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledRateCardRollupStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledRateCardRollupStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledRateCardRollupStatus.COMPLETED -> return next == ScheduledRateCardRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledRateCardRollupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rate card rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((createdAt.toDouble() - rate) * 100) / 100

    fun snapshot(): ScheduledRateCardRollup {
        return ScheduledRateCardRollup(
            code = code,
            name = name,
            rate = rate,
            region = region,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled rate card rollup in status planned. */
fun newScheduledRateCardRollup(code: String, name: String): ScheduledRateCardRollup {
    return ScheduledRateCardRollup(
        code = code,
        name = name,
        rate = 0.0,
        region = "",
        createdAt = 0,
        status = ScheduledRateCardRollupStatus.PLANNED,
    )
}
