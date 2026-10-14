package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered camera event rollup is in its life cycle. */
enum class TieredCameraEventRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered camera event rollup, with what the planners need to act on it. */
data class TieredCameraEventRollup(
    var code: String,
    var label: String,
    var maximum: Double,
    var lane: String,
    var sequence: Int,
    var description: String,
    var status: TieredCameraEventRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCameraEventRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCameraEventRollupStatus): Boolean {
        when (status) {
            TieredCameraEventRollupStatus.OPEN -> {
                when (next) {
                    TieredCameraEventRollupStatus.REOPENED -> return true
                }
                return false
            }
            TieredCameraEventRollupStatus.IN_REVIEW -> {
                when (next) {
                    TieredCameraEventRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredCameraEventRollupStatus.RESOLVED -> return next == TieredCameraEventRollupStatus.REOPENED || next == TieredCameraEventRollupStatus.ARCHIVED
            TieredCameraEventRollupStatus.REOPENED -> return next == TieredCameraEventRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredCameraEventRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered camera event rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered camera event rollup"} $code"

    fun effectiveLoad(): Double = round(maximum * sequence.toDouble() * 100) / 100

    fun snapshot(): TieredCameraEventRollup {
        return TieredCameraEventRollup(
            code = code,
            label = label,
            maximum = maximum,
            lane = lane,
            sequence = sequence,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered camera event rollup in status open. */
fun newTieredCameraEventRollup(code: String, label: String): TieredCameraEventRollup {
    return TieredCameraEventRollup(
        code = code,
        label = label,
        maximum = 0.0,
        lane = "",
        sequence = 0,
        description = "",
        status = TieredCameraEventRollupStatus.OPEN,
    )
}
