package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap checkpoint is in its life cycle. */
enum class SwapCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A swap checkpoint. Its code is unique within a tenant. */
data class SwapCheckpoint(
    var code: String,
    var name: String,
    var maximum: Double,
    var timezone: String,
    var threshold: Double,
    var margin: Double,
    var locale: String,
    var status: SwapCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapCheckpointStatus): Boolean {
        when (status) {
            SwapCheckpointStatus.OPEN -> {
                when (next) {
                    SwapCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            SwapCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    SwapCheckpointStatus.RESOLVED, SwapCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            SwapCheckpointStatus.RESOLVED -> return next == SwapCheckpointStatus.REOPENED || next == SwapCheckpointStatus.ARCHIVED
            SwapCheckpointStatus.REOPENED -> {
                when (next) {
                    SwapCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("swap checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"swap checkpoint"} $code"

    fun weightedVolume(): Double = round(maximum * margin * 100) / 100

    fun snapshot(): SwapCheckpoint {
        return SwapCheckpoint(
            code = code,
            name = name,
            maximum = maximum,
            timezone = timezone,
            threshold = threshold,
            margin = margin,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a swap checkpoint in status open. */
fun newSwapCheckpoint(code: String, name: String): SwapCheckpoint {
    return SwapCheckpoint(
        code = code,
        name = name,
        maximum = 0.0,
        timezone = "",
        threshold = 0.0,
        margin = 0.0,
        locale = "",
        status = SwapCheckpointStatus.OPEN,
    )
}
