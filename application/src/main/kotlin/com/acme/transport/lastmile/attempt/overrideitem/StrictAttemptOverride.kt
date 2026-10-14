package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict attempt override is in its life cycle. */
enum class StrictAttemptOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a strict attempt override and its current status. */
data class StrictAttemptOverride(
    var code: String,
    var label: String,
    var windowDays: Int,
    var visible: Boolean,
    var priority: Int,
    var routeCode: String,
    var archived: Boolean,
    var status: StrictAttemptOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAttemptOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAttemptOverrideStatus): Boolean {
        when (status) {
            StrictAttemptOverrideStatus.OPEN -> return next == StrictAttemptOverrideStatus.IN_REVIEW
            StrictAttemptOverrideStatus.IN_REVIEW -> return next == StrictAttemptOverrideStatus.RESOLVED || next == StrictAttemptOverrideStatus.REOPENED
            StrictAttemptOverrideStatus.RESOLVED -> return next == StrictAttemptOverrideStatus.REOPENED || next == StrictAttemptOverrideStatus.ARCHIVED
            StrictAttemptOverrideStatus.REOPENED -> {
                when (next) {
                    StrictAttemptOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAttemptOverrideStatus) {
        if (!canMoveTo(next)) {
            error("strict attempt override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((windowDays.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): StrictAttemptOverride {
        return StrictAttemptOverride(
            code = code,
            label = label,
            windowDays = windowDays,
            visible = visible,
            priority = priority,
            routeCode = routeCode,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict attempt override in status open. */
fun newStrictAttemptOverride(code: String, label: String): StrictAttemptOverride {
    return StrictAttemptOverride(
        code = code,
        label = label,
        windowDays = 0,
        visible = false,
        priority = 0,
        routeCode = "",
        archived = false,
        status = StrictAttemptOverrideStatus.OPEN,
    )
}
