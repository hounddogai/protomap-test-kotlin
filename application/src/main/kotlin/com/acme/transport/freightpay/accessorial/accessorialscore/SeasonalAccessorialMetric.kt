package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal accessorial metric is in its life cycle. */
enum class SeasonalAccessorialMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal accessorial metric. Its code is unique within a tenant. */
data class SeasonalAccessorialMetric(
    var code: String,
    var title: String,
    var unit: String,
    var weight: Double,
    var stopCount: Int,
    var description: String,
    var priority: Int,
    var archived: Boolean,
    var label: String,
    var status: SeasonalAccessorialMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAccessorialMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAccessorialMetricStatus): Boolean {
        when (status) {
            SeasonalAccessorialMetricStatus.OPEN -> {
                when (next) {
                    SeasonalAccessorialMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalAccessorialMetricStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalAccessorialMetricStatus.RESOLVED, SeasonalAccessorialMetricStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalAccessorialMetricStatus.RESOLVED -> return next == SeasonalAccessorialMetricStatus.ARCHIVED
            SeasonalAccessorialMetricStatus.REOPENED -> return next == SeasonalAccessorialMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalAccessorialMetricStatus) {
        if (!canMoveTo(next)) {
            error("seasonal accessorial metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedExposure(): Double = round((weight - priority.toDouble()) * 100) / 100

    fun snapshot(): SeasonalAccessorialMetric {
        return SeasonalAccessorialMetric(
            code = code,
            title = title,
            unit = unit,
            weight = weight,
            stopCount = stopCount,
            description = description,
            priority = priority,
            archived = archived,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal accessorial metric in status open. */
fun newSeasonalAccessorialMetric(code: String, title: String): SeasonalAccessorialMetric {
    return SeasonalAccessorialMetric(
        code = code,
        title = title,
        unit = "",
        weight = 0.0,
        stopCount = 0,
        description = "",
        priority = 0,
        archived = false,
        label = "",
        status = SeasonalAccessorialMetricStatus.OPEN,
    )
}
