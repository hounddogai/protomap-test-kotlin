package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled rest break target is in its life cycle. */
enum class ScheduledRestBreakTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled rest break target and its current status. */
data class ScheduledRestBreakTarget(
    var code: String,
    var title: String,
    var archived: Boolean,
    var active: Boolean,
    var threshold: Double,
    var pinned: Boolean,
    var label: String,
    var loadKg: Double,
    var odometer: Int,
    var status: ScheduledRestBreakTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakTargetStatus): Boolean {
        when (status) {
            ScheduledRestBreakTargetStatus.NEW -> return next == ScheduledRestBreakTargetStatus.DONE
            ScheduledRestBreakTargetStatus.QUEUED -> return next == ScheduledRestBreakTargetStatus.RUNNING
            ScheduledRestBreakTargetStatus.RUNNING -> return next == ScheduledRestBreakTargetStatus.DONE || next == ScheduledRestBreakTargetStatus.FAILED
            ScheduledRestBreakTargetStatus.DONE -> {
                when (next) {
                    ScheduledRestBreakTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakTargetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun adjustedLoad(): Double = round(odometer.toDouble() * loadKg * 100) / 100

    fun snapshot(): ScheduledRestBreakTarget {
        return ScheduledRestBreakTarget(
            code = code,
            title = title,
            archived = archived,
            active = active,
            threshold = threshold,
            pinned = pinned,
            label = label,
            loadKg = loadKg,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled rest break target in status new. */
fun newScheduledRestBreakTarget(code: String, title: String): ScheduledRestBreakTarget {
    return ScheduledRestBreakTarget(
        code = code,
        title = title,
        archived = false,
        active = false,
        threshold = 0.0,
        pinned = false,
        label = "",
        loadKg = 0.0,
        odometer = 0,
        status = ScheduledRestBreakTargetStatus.NEW,
    )
}
