package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dwell variance is in its life cycle. */
enum class WeightedDwellVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted dwell variance and its current status. */
data class WeightedDwellVariance(
    var code: String,
    var title: String,
    var driverId: String,
    var floorValue: Double,
    var budget: Double,
    var stopCount: Int,
    var archived: Boolean,
    var quantity: Int,
    var status: WeightedDwellVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDwellVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDwellVarianceStatus): Boolean {
        when (status) {
            WeightedDwellVarianceStatus.NEW -> return next == WeightedDwellVarianceStatus.QUEUED || next == WeightedDwellVarianceStatus.FAILED
            WeightedDwellVarianceStatus.QUEUED -> {
                when (next) {
                    WeightedDwellVarianceStatus.RUNNING, WeightedDwellVarianceStatus.FAILED -> return true
                }
                return false
            }
            WeightedDwellVarianceStatus.RUNNING -> return next == WeightedDwellVarianceStatus.FAILED
            WeightedDwellVarianceStatus.DONE -> return next == WeightedDwellVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedDwellVarianceStatus) {
        if (!canMoveTo(next)) {
            error("weighted dwell variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted dwell variance"} $code"

    fun adjustedVolume(): Double = round((budget - quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedDwellVariance {
        return WeightedDwellVariance(
            code = code,
            title = title,
            driverId = driverId,
            floorValue = floorValue,
            budget = budget,
            stopCount = stopCount,
            archived = archived,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted dwell variance in status new. */
fun newWeightedDwellVariance(code: String, title: String): WeightedDwellVariance {
    return WeightedDwellVariance(
        code = code,
        title = title,
        driverId = "",
        floorValue = 0.0,
        budget = 0.0,
        stopCount = 0,
        archived = false,
        quantity = 0,
        status = WeightedDwellVarianceStatus.NEW,
    )
}
