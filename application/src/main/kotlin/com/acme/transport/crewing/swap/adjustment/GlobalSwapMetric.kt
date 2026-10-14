package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global swap metric is in its life cycle. */
enum class GlobalSwapMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global swap metric. Its code is unique within a tenant. */
data class GlobalSwapMetric(
    var code: String,
    var title: String,
    var weight: Double,
    var lane: String,
    var updatedAt: Int,
    var status: GlobalSwapMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSwapMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSwapMetricStatus): Boolean {
        when (status) {
            GlobalSwapMetricStatus.IDLE -> return next == GlobalSwapMetricStatus.WARMING || next == GlobalSwapMetricStatus.READY
            GlobalSwapMetricStatus.WARMING -> return next == GlobalSwapMetricStatus.DRAINING
            GlobalSwapMetricStatus.READY -> return next == GlobalSwapMetricStatus.DRAINING || next == GlobalSwapMetricStatus.STOPPED
            GlobalSwapMetricStatus.DRAINING -> {
                when (next) {
                    GlobalSwapMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalSwapMetricStatus) {
        if (!canMoveTo(next)) {
            error("global swap metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global swap metric"} $code"

    fun adjustedLoad(): Double = round((weight - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalSwapMetric {
        return GlobalSwapMetric(
            code = code,
            title = title,
            weight = weight,
            lane = lane,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global swap metric in status idle. */
fun newGlobalSwapMetric(code: String, title: String): GlobalSwapMetric {
    return GlobalSwapMetric(
        code = code,
        title = title,
        weight = 0.0,
        lane = "",
        updatedAt = 0,
        status = GlobalSwapMetricStatus.IDLE,
    )
}
