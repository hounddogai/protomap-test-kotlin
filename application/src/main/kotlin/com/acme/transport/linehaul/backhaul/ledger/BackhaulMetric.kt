package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul metric is in its life cycle. */
enum class BackhaulMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One backhaul metric, with what the planners need to act on it. */
data class BackhaulMetric(
    var code: String,
    var title: String,
    var amount: Double,
    var updatedAt: Int,
    var sequence: Int,
    var shiftCode: String,
    var maximum: Double,
    var score: Double,
    var routeCode: String,
    var minimum: Double,
    var status: BackhaulMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulMetricStatus): Boolean {
        when (status) {
            BackhaulMetricStatus.DRAFT -> return next == BackhaulMetricStatus.SUSPENDED || next == BackhaulMetricStatus.CLOSED
            BackhaulMetricStatus.PENDING -> {
                when (next) {
                    BackhaulMetricStatus.ACTIVE -> return true
                }
                return false
            }
            BackhaulMetricStatus.ACTIVE -> {
                when (next) {
                    BackhaulMetricStatus.SUSPENDED, BackhaulMetricStatus.CLOSED -> return true
                }
                return false
            }
            BackhaulMetricStatus.SUSPENDED -> return next == BackhaulMetricStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BackhaulMetricStatus) {
        if (!canMoveTo(next)) {
            error("backhaul metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round((maximum - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): BackhaulMetric {
        return BackhaulMetric(
            code = code,
            title = title,
            amount = amount,
            updatedAt = updatedAt,
            sequence = sequence,
            shiftCode = shiftCode,
            maximum = maximum,
            score = score,
            routeCode = routeCode,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a backhaul metric in status draft. */
fun newBackhaulMetric(code: String, title: String): BackhaulMetric {
    return BackhaulMetric(
        code = code,
        title = title,
        amount = 0.0,
        updatedAt = 0,
        sequence = 0,
        shiftCode = "",
        maximum = 0.0,
        score = 0.0,
        routeCode = "",
        minimum = 0.0,
        status = BackhaulMetricStatus.DRAFT,
    )
}
