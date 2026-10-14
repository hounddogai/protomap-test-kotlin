package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire metric is in its life cycle. */
enum class TireMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tire metric as the tire team keeps it. */
data class TireMetric(
    var code: String,
    var title: String,
    var margin: Double,
    var locale: String,
    var expiresAt: Int,
    var status: TireMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireMetricStatus): Boolean {
        when (status) {
            TireMetricStatus.NEW -> return next == TireMetricStatus.RUNNING || next == TireMetricStatus.DONE
            TireMetricStatus.QUEUED -> return next == TireMetricStatus.RUNNING || next == TireMetricStatus.FAILED
            TireMetricStatus.RUNNING -> {
                when (next) {
                    TireMetricStatus.DONE, TireMetricStatus.FAILED -> return true
                }
                return false
            }
            TireMetricStatus.DONE -> return next == TireMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TireMetricStatus) {
        if (!canMoveTo(next)) {
            error("tire metric $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((margin - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TireMetric {
        return TireMetric(
            code = code,
            title = title,
            margin = margin,
            locale = locale,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tire metric in status new. */
fun newTireMetric(code: String, title: String): TireMetric {
    return TireMetric(
        code = code,
        title = title,
        margin = 0.0,
        locale = "",
        expiresAt = 0,
        status = TireMetricStatus.NEW,
    )
}
