package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff milestone is in its life cycle. */
enum class CutoffMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cutoff milestone. Its code is unique within a tenant. */
data class CutoffMilestone(
    var code: String,
    var name: String,
    var summary: String,
    var notes: String,
    var retries: Int,
    var externalRef: String,
    var updatedAt: Int,
    var stopCount: Int,
    var routeCode: String,
    var status: CutoffMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffMilestoneStatus): Boolean {
        when (status) {
            CutoffMilestoneStatus.IDLE -> {
                when (next) {
                    CutoffMilestoneStatus.DRAINING, CutoffMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            CutoffMilestoneStatus.WARMING -> return next == CutoffMilestoneStatus.READY
            CutoffMilestoneStatus.READY -> return next == CutoffMilestoneStatus.DRAINING || next == CutoffMilestoneStatus.STOPPED
            CutoffMilestoneStatus.DRAINING -> {
                when (next) {
                    CutoffMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("cutoff milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((stopCount.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): CutoffMilestone {
        return CutoffMilestone(
            code = code,
            name = name,
            summary = summary,
            notes = notes,
            retries = retries,
            externalRef = externalRef,
            updatedAt = updatedAt,
            stopCount = stopCount,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff milestone in status idle. */
fun newCutoffMilestone(code: String, name: String): CutoffMilestone {
    return CutoffMilestone(
        code = code,
        name = name,
        summary = "",
        notes = "",
        retries = 0,
        externalRef = "",
        updatedAt = 0,
        stopCount = 0,
        routeCode = "",
        status = CutoffMilestoneStatus.IDLE,
    )
}
