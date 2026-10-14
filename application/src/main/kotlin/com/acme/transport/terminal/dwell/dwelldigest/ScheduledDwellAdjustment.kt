package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dwell adjustment is in its life cycle. */
enum class ScheduledDwellAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled dwell adjustment and its current status. */
data class ScheduledDwellAdjustment(
    var code: String,
    var title: String,
    var sequence: Int,
    var updatedAt: Int,
    var minimum: Double,
    var lane: String,
    var status: ScheduledDwellAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDwellAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDwellAdjustmentStatus): Boolean {
        when (status) {
            ScheduledDwellAdjustmentStatus.DRAFT -> return next == ScheduledDwellAdjustmentStatus.PENDING || next == ScheduledDwellAdjustmentStatus.SUSPENDED
            ScheduledDwellAdjustmentStatus.PENDING -> {
                when (next) {
                    ScheduledDwellAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledDwellAdjustmentStatus.ACTIVE -> return next == ScheduledDwellAdjustmentStatus.SUSPENDED || next == ScheduledDwellAdjustmentStatus.CLOSED
            ScheduledDwellAdjustmentStatus.SUSPENDED -> {
                when (next) {
                    ScheduledDwellAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDwellAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dwell adjustment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round(updatedAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): ScheduledDwellAdjustment {
        return ScheduledDwellAdjustment(
            code = code,
            title = title,
            sequence = sequence,
            updatedAt = updatedAt,
            minimum = minimum,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled dwell adjustment in status draft. */
fun newScheduledDwellAdjustment(code: String, title: String): ScheduledDwellAdjustment {
    return ScheduledDwellAdjustment(
        code = code,
        title = title,
        sequence = 0,
        updatedAt = 0,
        minimum = 0.0,
        lane = "",
        status = ScheduledDwellAdjustmentStatus.DRAFT,
    )
}
