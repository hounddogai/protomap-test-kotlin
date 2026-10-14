package com.acme.transport.network.cutoff.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cutoff baseline records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff baseline is in its life cycle. */
enum class CutoffBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One cutoff baseline, with what the planners need to act on it. */
data class CutoffBaseline(
    var code: String,
    var label: String,
    var threshold: Double,
    var stopCount: Int,
    var graceDays: Int,
    var ratio: Double,
    var active: Boolean,
    var minimum: Double,
    var pinned: Boolean,
    var status: CutoffBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffBaselineStatus): Boolean {
        when (status) {
            CutoffBaselineStatus.OPEN -> return next == CutoffBaselineStatus.REOPENED
            CutoffBaselineStatus.IN_REVIEW -> {
                when (next) {
                    CutoffBaselineStatus.RESOLVED, CutoffBaselineStatus.REOPENED -> return true
                }
                return false
            }
            CutoffBaselineStatus.RESOLVED -> {
                when (next) {
                    CutoffBaselineStatus.REOPENED, CutoffBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            CutoffBaselineStatus.REOPENED -> {
                when (next) {
                    CutoffBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffBaselineStatus) {
        if (!canMoveTo(next)) {
            error("cutoff baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(graceDays.toDouble() * ratio * 100) / 100

    fun snapshot(): CutoffBaseline {
        return CutoffBaseline(
            code = code,
            label = label,
            threshold = threshold,
            stopCount = stopCount,
            graceDays = graceDays,
            ratio = ratio,
            active = active,
            minimum = minimum,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff baseline in status open. */
fun newCutoffBaseline(code: String, label: String): CutoffBaseline {
    return CutoffBaseline(
        code = code,
        label = label,
        threshold = 0.0,
        stopCount = 0,
        graceDays = 0,
        ratio = 0.0,
        active = false,
        minimum = 0.0,
        pinned = false,
        status = CutoffBaselineStatus.OPEN,
    )
}
