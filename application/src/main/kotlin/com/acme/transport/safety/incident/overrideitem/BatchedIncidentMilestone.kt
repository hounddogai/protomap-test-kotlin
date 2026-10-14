package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched incident milestone is in its life cycle. */
enum class BatchedIncidentMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched incident milestone. Its code is unique within a tenant. */
data class BatchedIncidentMilestone(
    var code: String,
    var title: String,
    var threshold: Double,
    var trailerId: String,
    var active: Boolean,
    var windowDays: Int,
    var status: BatchedIncidentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIncidentMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIncidentMilestoneStatus): Boolean {
        when (status) {
            BatchedIncidentMilestoneStatus.PLANNED -> {
                when (next) {
                    BatchedIncidentMilestoneStatus.SCHEDULED, BatchedIncidentMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedIncidentMilestoneStatus.SCHEDULED -> return next == BatchedIncidentMilestoneStatus.IN_PROGRESS || next == BatchedIncidentMilestoneStatus.COMPLETED
            BatchedIncidentMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedIncidentMilestoneStatus.COMPLETED, BatchedIncidentMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedIncidentMilestoneStatus.COMPLETED -> {
                when (next) {
                    BatchedIncidentMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedIncidentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched incident milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched incident milestone"} $code"

    fun netExposure(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedIncidentMilestone {
        return BatchedIncidentMilestone(
            code = code,
            title = title,
            threshold = threshold,
            trailerId = trailerId,
            active = active,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched incident milestone in status planned. */
fun newBatchedIncidentMilestone(code: String, title: String): BatchedIncidentMilestone {
    return BatchedIncidentMilestone(
        code = code,
        title = title,
        threshold = 0.0,
        trailerId = "",
        active = false,
        windowDays = 0,
        status = BatchedIncidentMilestoneStatus.PLANNED,
    )
}
