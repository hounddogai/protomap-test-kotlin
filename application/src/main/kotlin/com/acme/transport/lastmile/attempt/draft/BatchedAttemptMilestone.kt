package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched attempt milestone is in its life cycle. */
enum class BatchedAttemptMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched attempt milestone. Its code is unique within a tenant. */
data class BatchedAttemptMilestone(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var quantity: Int,
    var weight: Double,
    var pallets: Int,
    var distanceKm: Double,
    var active: Boolean,
    var createdAt: Int,
    var status: BatchedAttemptMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAttemptMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAttemptMilestoneStatus): Boolean {
        when (status) {
            BatchedAttemptMilestoneStatus.NEW -> {
                when (next) {
                    BatchedAttemptMilestoneStatus.RUNNING, BatchedAttemptMilestoneStatus.FAILED -> return true
                }
                return false
            }
            BatchedAttemptMilestoneStatus.QUEUED -> return next == BatchedAttemptMilestoneStatus.DONE
            BatchedAttemptMilestoneStatus.RUNNING -> return next == BatchedAttemptMilestoneStatus.DONE || next == BatchedAttemptMilestoneStatus.FAILED
            BatchedAttemptMilestoneStatus.DONE -> {
                when (next) {
                    BatchedAttemptMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAttemptMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched attempt milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((weight + pallets.toDouble()) * 100) / 100

    fun snapshot(): BatchedAttemptMilestone {
        return BatchedAttemptMilestone(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            quantity = quantity,
            weight = weight,
            pallets = pallets,
            distanceKm = distanceKm,
            active = active,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched attempt milestone in status new. */
fun newBatchedAttemptMilestone(code: String, name: String): BatchedAttemptMilestone {
    return BatchedAttemptMilestone(
        code = code,
        name = name,
        etaMinutes = 0,
        quantity = 0,
        weight = 0.0,
        pallets = 0,
        distanceKm = 0.0,
        active = false,
        createdAt = 0,
        status = BatchedAttemptMilestoneStatus.NEW,
    )
}
