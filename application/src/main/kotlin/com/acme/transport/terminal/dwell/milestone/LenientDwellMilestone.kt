package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dwell milestone records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a lenient dwell milestone is in its life cycle. */
enum class LenientDwellMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient dwell milestone and its current status. */
data class LenientDwellMilestone(
    var code: String,
    var label: String,
    var minimum: Double,
    var trailerId: String,
    var retries: Int,
    var status: LenientDwellMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellMilestoneStatus): Boolean {
        when (status) {
            LenientDwellMilestoneStatus.NEW -> {
                when (next) {
                    LenientDwellMilestoneStatus.QUEUED, LenientDwellMilestoneStatus.RUNNING -> return true
                }
                return false
            }
            LenientDwellMilestoneStatus.QUEUED -> {
                when (next) {
                    LenientDwellMilestoneStatus.FAILED -> return true
                }
                return false
            }
            LenientDwellMilestoneStatus.RUNNING -> return next == LenientDwellMilestoneStatus.DONE || next == LenientDwellMilestoneStatus.FAILED
            LenientDwellMilestoneStatus.DONE -> {
                when (next) {
                    LenientDwellMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDwellMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((retries.toDouble() + minimum) * 100) / 100

    fun snapshot(): LenientDwellMilestone {
        return LenientDwellMilestone(
            code = code,
            label = label,
            minimum = minimum,
            trailerId = trailerId,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient dwell milestone in status new. */
fun newLenientDwellMilestone(code: String, label: String): LenientDwellMilestone {
    return LenientDwellMilestone(
        code = code,
        label = label,
        minimum = 0.0,
        trailerId = "",
        retries = 0,
        status = LenientDwellMilestoneStatus.NEW,
    )
}
