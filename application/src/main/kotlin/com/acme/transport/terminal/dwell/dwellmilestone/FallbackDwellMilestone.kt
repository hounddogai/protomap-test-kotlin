package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback dwell milestone records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a fallback dwell milestone is in its life cycle. */
enum class FallbackDwellMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback dwell milestone, with what the planners need to act on it. */
data class FallbackDwellMilestone(
    var code: String,
    var name: String,
    var driverId: String,
    var shiftCode: String,
    var loadKg: Double,
    var phone: String,
    var expiresAt: Int,
    var durationMinutes: Int,
    var archived: Boolean,
    var status: FallbackDwellMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellMilestoneStatus): Boolean {
        when (status) {
            FallbackDwellMilestoneStatus.NEW -> return next == FallbackDwellMilestoneStatus.QUEUED || next == FallbackDwellMilestoneStatus.FAILED
            FallbackDwellMilestoneStatus.QUEUED -> return next == FallbackDwellMilestoneStatus.DONE || next == FallbackDwellMilestoneStatus.FAILED
            FallbackDwellMilestoneStatus.RUNNING -> return next == FallbackDwellMilestoneStatus.DONE
            FallbackDwellMilestoneStatus.DONE -> return next == FallbackDwellMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackDwellMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback dwell milestone"} $code"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() + loadKg) * 100) / 100

    fun snapshot(): FallbackDwellMilestone {
        return FallbackDwellMilestone(
            code = code,
            name = name,
            driverId = driverId,
            shiftCode = shiftCode,
            loadKg = loadKg,
            phone = phone,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback dwell milestone in status new. */
fun newFallbackDwellMilestone(code: String, name: String): FallbackDwellMilestone {
    return FallbackDwellMilestone(
        code = code,
        name = name,
        driverId = "",
        shiftCode = "",
        loadKg = 0.0,
        phone = "",
        expiresAt = 0,
        durationMinutes = 0,
        archived = false,
        status = FallbackDwellMilestoneStatus.NEW,
    )
}
