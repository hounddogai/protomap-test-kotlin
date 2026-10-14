package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict dwell adjustment records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a strict dwell adjustment is in its life cycle. */
enum class StrictDwellAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict dwell adjustment. Its code is unique within a tenant. */
data class StrictDwellAdjustment(
    var code: String,
    var label: String,
    var visible: Boolean,
    var country: String,
    var windowDays: Int,
    var endsAt: Int,
    var tags: MutableList<String>,
    var status: StrictDwellAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDwellAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDwellAdjustmentStatus): Boolean {
        when (status) {
            StrictDwellAdjustmentStatus.OPEN -> return next == StrictDwellAdjustmentStatus.IN_REVIEW
            StrictDwellAdjustmentStatus.IN_REVIEW -> return next == StrictDwellAdjustmentStatus.ARCHIVED
            StrictDwellAdjustmentStatus.RESOLVED -> {
                when (next) {
                    StrictDwellAdjustmentStatus.REOPENED, StrictDwellAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDwellAdjustmentStatus.REOPENED -> return next == StrictDwellAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDwellAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("strict dwell adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict dwell adjustment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((endsAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): StrictDwellAdjustment {
        return StrictDwellAdjustment(
            code = code,
            label = label,
            visible = visible,
            country = country,
            windowDays = windowDays,
            endsAt = endsAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict dwell adjustment in status open. */
fun newStrictDwellAdjustment(code: String, label: String): StrictDwellAdjustment {
    return StrictDwellAdjustment(
        code = code,
        label = label,
        visible = false,
        country = "",
        windowDays = 0,
        endsAt = 0,
        tags = mutableListOf<String>(),
        status = StrictDwellAdjustmentStatus.OPEN,
    )
}
