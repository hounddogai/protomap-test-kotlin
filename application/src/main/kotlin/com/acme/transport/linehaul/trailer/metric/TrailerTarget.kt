package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer target is in its life cycle. */
enum class TrailerTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One trailer target, with what the planners need to act on it. */
data class TrailerTarget(
    var code: String,
    var label: String,
    var batchSize: Int,
    var dock: String,
    var region: String,
    var depot: String,
    var retries: Int,
    var title: String,
    var status: TrailerTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerTargetStatus): Boolean {
        when (status) {
            TrailerTargetStatus.IDLE -> {
                when (next) {
                    TrailerTargetStatus.DRAINING, TrailerTargetStatus.STOPPED -> return true
                }
                return false
            }
            TrailerTargetStatus.WARMING -> {
                when (next) {
                    TrailerTargetStatus.DRAINING, TrailerTargetStatus.STOPPED -> return true
                }
                return false
            }
            TrailerTargetStatus.READY -> {
                when (next) {
                    TrailerTargetStatus.DRAINING -> return true
                }
                return false
            }
            TrailerTargetStatus.DRAINING -> return next == TrailerTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerTargetStatus) {
        if (!canMoveTo(next)) {
            error("trailer target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((retries.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): TrailerTarget {
        return TrailerTarget(
            code = code,
            label = label,
            batchSize = batchSize,
            dock = dock,
            region = region,
            depot = depot,
            retries = retries,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a trailer target in status idle. */
fun newTrailerTarget(code: String, label: String): TrailerTarget {
    return TrailerTarget(
        code = code,
        label = label,
        batchSize = 0,
        dock = "",
        region = "",
        depot = "",
        retries = 0,
        title = "",
        status = TrailerTargetStatus.IDLE,
    )
}
