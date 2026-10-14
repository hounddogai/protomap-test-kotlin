package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched qualification milestone is in its life cycle. */
enum class BatchedQualificationMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched qualification milestone and its current status. */
data class BatchedQualificationMilestone(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var trailerId: String,
    var timezone: String,
    var discount: Double,
    var status: BatchedQualificationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationMilestoneStatus): Boolean {
        when (status) {
            BatchedQualificationMilestoneStatus.NEW -> {
                when (next) {
                    BatchedQualificationMilestoneStatus.QUEUED, BatchedQualificationMilestoneStatus.RUNNING -> return true
                }
                return false
            }
            BatchedQualificationMilestoneStatus.QUEUED -> {
                when (next) {
                    BatchedQualificationMilestoneStatus.DONE, BatchedQualificationMilestoneStatus.FAILED -> return true
                }
                return false
            }
            BatchedQualificationMilestoneStatus.RUNNING -> return next == BatchedQualificationMilestoneStatus.DONE || next == BatchedQualificationMilestoneStatus.FAILED
            BatchedQualificationMilestoneStatus.DONE -> {
                when (next) {
                    BatchedQualificationMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedQualificationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedLoad(): Double = round(fuelLiters * discount * 100) / 100

    fun snapshot(): BatchedQualificationMilestone {
        return BatchedQualificationMilestone(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            trailerId = trailerId,
            timezone = timezone,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched qualification milestone in status new. */
fun newBatchedQualificationMilestone(code: String, title: String): BatchedQualificationMilestone {
    return BatchedQualificationMilestone(
        code = code,
        title = title,
        fuelLiters = 0.0,
        trailerId = "",
        timezone = "",
        discount = 0.0,
        status = BatchedQualificationMilestoneStatus.NEW,
    )
}
