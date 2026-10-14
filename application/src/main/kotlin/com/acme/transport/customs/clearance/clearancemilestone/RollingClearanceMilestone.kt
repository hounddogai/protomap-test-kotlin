package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling clearance milestone records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a rolling clearance milestone is in its life cycle. */
enum class RollingClearanceMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling clearance milestone, with what the planners need to act on it. */
data class RollingClearanceMilestone(
    var code: String,
    var label: String,
    var batchSize: Int,
    var rate: Double,
    var loadKg: Double,
    var score: Double,
    var status: RollingClearanceMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingClearanceMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingClearanceMilestoneStatus): Boolean {
        when (status) {
            RollingClearanceMilestoneStatus.PROPOSED -> {
                when (next) {
                    RollingClearanceMilestoneStatus.APPROVED -> return true
                }
                return false
            }
            RollingClearanceMilestoneStatus.APPROVED -> return next == RollingClearanceMilestoneStatus.WITHDRAWN
            RollingClearanceMilestoneStatus.REJECTED -> return next == RollingClearanceMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingClearanceMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling clearance milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling clearance milestone"} $code"

    fun effectiveVolume(): Double = round((batchSize.toDouble() + loadKg) * 100) / 100

    fun snapshot(): RollingClearanceMilestone {
        return RollingClearanceMilestone(
            code = code,
            label = label,
            batchSize = batchSize,
            rate = rate,
            loadKg = loadKg,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling clearance milestone in status proposed. */
fun newRollingClearanceMilestone(code: String, label: String): RollingClearanceMilestone {
    return RollingClearanceMilestone(
        code = code,
        label = label,
        batchSize = 0,
        rate = 0.0,
        loadKg = 0.0,
        score = 0.0,
        status = RollingClearanceMilestoneStatus.PROPOSED,
    )
}
