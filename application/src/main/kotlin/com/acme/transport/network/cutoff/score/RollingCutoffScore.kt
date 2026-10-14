package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling cutoff score records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a rolling cutoff score is in its life cycle. */
enum class RollingCutoffScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling cutoff score. Its code is unique within a tenant. */
data class RollingCutoffScore(
    var code: String,
    var label: String,
    var pallets: Int,
    var maximum: Double,
    var segment: String,
    var ceiling: Double,
    var odometer: Int,
    var priority: Int,
    var minimum: Double,
    var dock: String,
    var floorValue: Double,
    var status: RollingCutoffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffScoreStatus): Boolean {
        when (status) {
            RollingCutoffScoreStatus.OPEN -> return next == RollingCutoffScoreStatus.REOPENED
            RollingCutoffScoreStatus.IN_REVIEW -> return next == RollingCutoffScoreStatus.RESOLVED
            RollingCutoffScoreStatus.RESOLVED -> return next == RollingCutoffScoreStatus.REOPENED || next == RollingCutoffScoreStatus.ARCHIVED
            RollingCutoffScoreStatus.REOPENED -> {
                when (next) {
                    RollingCutoffScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCutoffScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((maximum + priority.toDouble()) * 100) / 100

    fun snapshot(): RollingCutoffScore {
        return RollingCutoffScore(
            code = code,
            label = label,
            pallets = pallets,
            maximum = maximum,
            segment = segment,
            ceiling = ceiling,
            odometer = odometer,
            priority = priority,
            minimum = minimum,
            dock = dock,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling cutoff score in status open. */
fun newRollingCutoffScore(code: String, label: String): RollingCutoffScore {
    return RollingCutoffScore(
        code = code,
        label = label,
        pallets = 0,
        maximum = 0.0,
        segment = "",
        ceiling = 0.0,
        odometer = 0,
        priority = 0,
        minimum = 0.0,
        dock = "",
        floorValue = 0.0,
        status = RollingCutoffScoreStatus.OPEN,
    )
}
