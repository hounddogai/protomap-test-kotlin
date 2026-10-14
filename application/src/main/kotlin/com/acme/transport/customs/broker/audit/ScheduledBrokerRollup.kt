package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled broker rollup is in its life cycle. */
enum class ScheduledBrokerRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled broker rollup as the broker team keeps it. */
data class ScheduledBrokerRollup(
    var code: String,
    var title: String,
    var ratio: Double,
    var ceiling: Double,
    var batchSize: Int,
    var depot: String,
    var status: ScheduledBrokerRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBrokerRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBrokerRollupStatus): Boolean {
        when (status) {
            ScheduledBrokerRollupStatus.OPEN -> return next == ScheduledBrokerRollupStatus.RESOLVED
            ScheduledBrokerRollupStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledBrokerRollupStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledBrokerRollupStatus.RESOLVED -> {
                when (next) {
                    ScheduledBrokerRollupStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledBrokerRollupStatus.REOPENED -> return next == ScheduledBrokerRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledBrokerRollupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled broker rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((ratio - ceiling) * 100) / 100

    fun snapshot(): ScheduledBrokerRollup {
        return ScheduledBrokerRollup(
            code = code,
            title = title,
            ratio = ratio,
            ceiling = ceiling,
            batchSize = batchSize,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled broker rollup in status open. */
fun newScheduledBrokerRollup(code: String, title: String): ScheduledBrokerRollup {
    return ScheduledBrokerRollup(
        code = code,
        title = title,
        ratio = 0.0,
        ceiling = 0.0,
        batchSize = 0,
        depot = "",
        status = ScheduledBrokerRollupStatus.OPEN,
    )
}
