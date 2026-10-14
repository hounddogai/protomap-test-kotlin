package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict camera event cycle is in its life cycle. */
enum class StrictCameraEventCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict camera event cycle and its current status. */
data class StrictCameraEventCycle(
    var code: String,
    var title: String,
    var ratio: Double,
    var pinned: Boolean,
    var startsAt: Int,
    var ceiling: Double,
    var status: StrictCameraEventCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCameraEventCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCameraEventCycleStatus): Boolean {
        when (status) {
            StrictCameraEventCycleStatus.DRAFT -> {
                when (next) {
                    StrictCameraEventCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictCameraEventCycleStatus.PENDING -> {
                when (next) {
                    StrictCameraEventCycleStatus.SUSPENDED, StrictCameraEventCycleStatus.CLOSED -> return true
                }
                return false
            }
            StrictCameraEventCycleStatus.ACTIVE -> return next == StrictCameraEventCycleStatus.SUSPENDED || next == StrictCameraEventCycleStatus.CLOSED
            StrictCameraEventCycleStatus.SUSPENDED -> {
                when (next) {
                    StrictCameraEventCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCameraEventCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict camera event cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((startsAt.toDouble() - ratio) * 100) / 100

    fun snapshot(): StrictCameraEventCycle {
        return StrictCameraEventCycle(
            code = code,
            title = title,
            ratio = ratio,
            pinned = pinned,
            startsAt = startsAt,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict camera event cycle in status draft. */
fun newStrictCameraEventCycle(code: String, title: String): StrictCameraEventCycle {
    return StrictCameraEventCycle(
        code = code,
        title = title,
        ratio = 0.0,
        pinned = false,
        startsAt = 0,
        ceiling = 0.0,
        status = StrictCameraEventCycleStatus.DRAFT,
    )
}
