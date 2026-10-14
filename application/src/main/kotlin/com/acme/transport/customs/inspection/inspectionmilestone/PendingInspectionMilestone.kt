package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection milestone is in its life cycle. */
enum class PendingInspectionMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending inspection milestone. Its code is unique within a tenant. */
data class PendingInspectionMilestone(
    var code: String,
    var label: String,
    var batchSize: Int,
    var endsAt: Int,
    var channel: String,
    var description: String,
    var budget: Double,
    var priority: Int,
    var phone: String,
    var summary: String,
    var status: PendingInspectionMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionMilestoneStatus): Boolean {
        when (status) {
            PendingInspectionMilestoneStatus.NEW -> {
                when (next) {
                    PendingInspectionMilestoneStatus.RUNNING, PendingInspectionMilestoneStatus.DONE -> return true
                }
                return false
            }
            PendingInspectionMilestoneStatus.QUEUED -> {
                when (next) {
                    PendingInspectionMilestoneStatus.DONE, PendingInspectionMilestoneStatus.FAILED -> return true
                }
                return false
            }
            PendingInspectionMilestoneStatus.RUNNING -> return next == PendingInspectionMilestoneStatus.DONE || next == PendingInspectionMilestoneStatus.FAILED
            PendingInspectionMilestoneStatus.DONE -> {
                when (next) {
                    PendingInspectionMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round((budget + batchSize.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionMilestone {
        return PendingInspectionMilestone(
            code = code,
            label = label,
            batchSize = batchSize,
            endsAt = endsAt,
            channel = channel,
            description = description,
            budget = budget,
            priority = priority,
            phone = phone,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending inspection milestone in status new. */
fun newPendingInspectionMilestone(code: String, label: String): PendingInspectionMilestone {
    return PendingInspectionMilestone(
        code = code,
        label = label,
        batchSize = 0,
        endsAt = 0,
        channel = "",
        description = "",
        budget = 0.0,
        priority = 0,
        phone = "",
        summary = "",
        status = PendingInspectionMilestoneStatus.NEW,
    )
}
