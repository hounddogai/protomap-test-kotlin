package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending capacity metric is in its life cycle. */
enum class PendingCapacityMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending capacity metric, with what the planners need to act on it. */
data class PendingCapacityMetric(
    var code: String,
    var title: String,
    var score: Double,
    var minimum: Double,
    var graceDays: Int,
    var distanceKm: Double,
    var amount: Double,
    var status: PendingCapacityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCapacityMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCapacityMetricStatus): Boolean {
        when (status) {
            PendingCapacityMetricStatus.REQUESTED -> {
                when (next) {
                    PendingCapacityMetricStatus.REVOKED -> return true
                }
                return false
            }
            PendingCapacityMetricStatus.GRANTED -> return next == PendingCapacityMetricStatus.REVOKED
            PendingCapacityMetricStatus.REVOKED -> {
                when (next) {
                    PendingCapacityMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCapacityMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending capacity metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round(minimum * distanceKm * 100) / 100

    fun snapshot(): PendingCapacityMetric {
        return PendingCapacityMetric(
            code = code,
            title = title,
            score = score,
            minimum = minimum,
            graceDays = graceDays,
            distanceKm = distanceKm,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending capacity metric in status requested. */
fun newPendingCapacityMetric(code: String, title: String): PendingCapacityMetric {
    return PendingCapacityMetric(
        code = code,
        title = title,
        score = 0.0,
        minimum = 0.0,
        graceDays = 0,
        distanceKm = 0.0,
        amount = 0.0,
        status = PendingCapacityMetricStatus.REQUESTED,
    )
}
