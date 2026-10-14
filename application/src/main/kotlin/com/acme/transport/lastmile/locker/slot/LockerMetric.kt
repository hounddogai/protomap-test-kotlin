package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker metric is in its life cycle. */
enum class LockerMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A locker metric as the locker team keeps it. */
data class LockerMetric(
    var code: String,
    var title: String,
    var graceDays: Int,
    var notes: String,
    var cost: Double,
    var startsAt: Int,
    var shiftCode: String,
    var rate: Double,
    var depot: String,
    var weight: Double,
    var status: LockerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerMetricStatus): Boolean {
        when (status) {
            LockerMetricStatus.DRAFT -> {
                when (next) {
                    LockerMetricStatus.CLOSED -> return true
                }
                return false
            }
            LockerMetricStatus.PENDING -> return next == LockerMetricStatus.ACTIVE || next == LockerMetricStatus.CLOSED
            LockerMetricStatus.ACTIVE -> return next == LockerMetricStatus.SUSPENDED
            LockerMetricStatus.SUSPENDED -> return next == LockerMetricStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LockerMetricStatus) {
        if (!canMoveTo(next)) {
            error("locker metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round(startsAt.toDouble() * weight * 100) / 100

    fun snapshot(): LockerMetric {
        return LockerMetric(
            code = code,
            title = title,
            graceDays = graceDays,
            notes = notes,
            cost = cost,
            startsAt = startsAt,
            shiftCode = shiftCode,
            rate = rate,
            depot = depot,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a locker metric in status draft. */
fun newLockerMetric(code: String, title: String): LockerMetric {
    return LockerMetric(
        code = code,
        title = title,
        graceDays = 0,
        notes = "",
        cost = 0.0,
        startsAt = 0,
        shiftCode = "",
        rate = 0.0,
        depot = "",
        weight = 0.0,
        status = LockerMetricStatus.DRAFT,
    )
}
