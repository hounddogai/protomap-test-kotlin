package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted sortation estimate is in its life cycle. */
enum class WeightedSortationEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted sortation estimate as the sortation team keeps it. */
data class WeightedSortationEstimate(
    var code: String,
    var label: String,
    var lane: String,
    var createdAt: Int,
    var version: Int,
    var visible: Boolean,
    var budget: Double,
    var status: WeightedSortationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSortationEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSortationEstimateStatus): Boolean {
        when (status) {
            WeightedSortationEstimateStatus.DRAFT -> return next == WeightedSortationEstimateStatus.PENDING || next == WeightedSortationEstimateStatus.SUSPENDED
            WeightedSortationEstimateStatus.PENDING -> {
                when (next) {
                    WeightedSortationEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedSortationEstimateStatus.ACTIVE -> return next == WeightedSortationEstimateStatus.SUSPENDED
            WeightedSortationEstimateStatus.SUSPENDED -> return next == WeightedSortationEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedSortationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("weighted sortation estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedTotal(): Double = round((version.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedSortationEstimate {
        return WeightedSortationEstimate(
            code = code,
            label = label,
            lane = lane,
            createdAt = createdAt,
            version = version,
            visible = visible,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted sortation estimate in status draft. */
fun newWeightedSortationEstimate(code: String, label: String): WeightedSortationEstimate {
    return WeightedSortationEstimate(
        code = code,
        label = label,
        lane = "",
        createdAt = 0,
        version = 0,
        visible = false,
        budget = 0.0,
        status = WeightedSortationEstimateStatus.DRAFT,
    )
}
