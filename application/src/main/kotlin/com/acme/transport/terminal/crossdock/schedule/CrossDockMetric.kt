package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock metric is in its life cycle. */
enum class CrossDockMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cross dock metric and its current status. */
data class CrossDockMetric(
    var code: String,
    var label: String,
    var retries: Int,
    var expiresAt: Int,
    var odometer: Int,
    var startsAt: Int,
    var visible: Boolean,
    var country: String,
    var cost: Double,
    var ceiling: Double,
    var summary: String,
    var status: CrossDockMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockMetricStatus): Boolean {
        when (status) {
            CrossDockMetricStatus.DRAFT -> return next == CrossDockMetricStatus.ACTIVE || next == CrossDockMetricStatus.SUSPENDED
            CrossDockMetricStatus.PENDING -> return next == CrossDockMetricStatus.ACTIVE || next == CrossDockMetricStatus.SUSPENDED
            CrossDockMetricStatus.ACTIVE -> {
                when (next) {
                    CrossDockMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            CrossDockMetricStatus.SUSPENDED -> {
                when (next) {
                    CrossDockMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockMetricStatus) {
        if (!canMoveTo(next)) {
            error("cross dock metric $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"cross dock metric"} $code"

    fun projectedValue(): Double = round((cost + odometer.toDouble()) * 100) / 100

    fun snapshot(): CrossDockMetric {
        return CrossDockMetric(
            code = code,
            label = label,
            retries = retries,
            expiresAt = expiresAt,
            odometer = odometer,
            startsAt = startsAt,
            visible = visible,
            country = country,
            cost = cost,
            ceiling = ceiling,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a cross dock metric in status draft. */
fun newCrossDockMetric(code: String, label: String): CrossDockMetric {
    return CrossDockMetric(
        code = code,
        label = label,
        retries = 0,
        expiresAt = 0,
        odometer = 0,
        startsAt = 0,
        visible = false,
        country = "",
        cost = 0.0,
        ceiling = 0.0,
        summary = "",
        status = CrossDockMetricStatus.DRAFT,
    )
}
