package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered locker metric is in its life cycle. */
enum class TieredLockerMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered locker metric as the locker team keeps it. */
data class TieredLockerMetric(
    var code: String,
    var name: String,
    var contactEmail: String,
    var depot: String,
    var budget: Double,
    var updatedAt: Int,
    var status: TieredLockerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredLockerMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredLockerMetricStatus): Boolean {
        when (status) {
            TieredLockerMetricStatus.OPEN -> return next == TieredLockerMetricStatus.IN_REVIEW || next == TieredLockerMetricStatus.REOPENED
            TieredLockerMetricStatus.IN_REVIEW -> return next == TieredLockerMetricStatus.RESOLVED || next == TieredLockerMetricStatus.ARCHIVED
            TieredLockerMetricStatus.RESOLVED -> return next == TieredLockerMetricStatus.REOPENED || next == TieredLockerMetricStatus.ARCHIVED
            TieredLockerMetricStatus.REOPENED -> {
                when (next) {
                    TieredLockerMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredLockerMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered locker metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered locker metric"} $code"

    fun adjustedLoad(): Double = round((budget + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TieredLockerMetric {
        return TieredLockerMetric(
            code = code,
            name = name,
            contactEmail = contactEmail,
            depot = depot,
            budget = budget,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered locker metric in status open. */
fun newTieredLockerMetric(code: String, name: String): TieredLockerMetric {
    return TieredLockerMetric(
        code = code,
        name = name,
        contactEmail = "",
        depot = "",
        budget = 0.0,
        updatedAt = 0,
        status = TieredLockerMetricStatus.OPEN,
    )
}
