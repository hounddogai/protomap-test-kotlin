package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal rest break estimate is in its life cycle. */
enum class SeasonalRestBreakEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal rest break estimate as the rest break team keeps it. */
data class SeasonalRestBreakEstimate(
    var code: String,
    var name: String,
    var margin: Double,
    var minimum: Double,
    var pinned: Boolean,
    var source: String,
    var loadKg: Double,
    var summary: String,
    var capacity: Int,
    var expiresAt: Int,
    var status: SeasonalRestBreakEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRestBreakEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRestBreakEstimateStatus): Boolean {
        when (status) {
            SeasonalRestBreakEstimateStatus.PLANNED -> return next == SeasonalRestBreakEstimateStatus.IN_PROGRESS || next == SeasonalRestBreakEstimateStatus.COMPLETED
            SeasonalRestBreakEstimateStatus.SCHEDULED -> return next == SeasonalRestBreakEstimateStatus.IN_PROGRESS || next == SeasonalRestBreakEstimateStatus.COMPLETED
            SeasonalRestBreakEstimateStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalRestBreakEstimateStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalRestBreakEstimateStatus.COMPLETED -> {
                when (next) {
                    SeasonalRestBreakEstimateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRestBreakEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal rest break estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round(minimum * margin * 100) / 100

    fun snapshot(): SeasonalRestBreakEstimate {
        return SeasonalRestBreakEstimate(
            code = code,
            name = name,
            margin = margin,
            minimum = minimum,
            pinned = pinned,
            source = source,
            loadKg = loadKg,
            summary = summary,
            capacity = capacity,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal rest break estimate in status planned. */
fun newSeasonalRestBreakEstimate(code: String, name: String): SeasonalRestBreakEstimate {
    return SeasonalRestBreakEstimate(
        code = code,
        name = name,
        margin = 0.0,
        minimum = 0.0,
        pinned = false,
        source = "",
        loadKg = 0.0,
        summary = "",
        capacity = 0,
        expiresAt = 0,
        status = SeasonalRestBreakEstimateStatus.PLANNED,
    )
}
