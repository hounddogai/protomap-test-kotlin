package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched audit milestone is in its life cycle. */
enum class BatchedAuditMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched audit milestone and its current status. */
data class BatchedAuditMilestone(
    var code: String,
    var title: String,
    var minimum: Double,
    var segment: String,
    var pallets: Int,
    var contactEmail: String,
    var loadKg: Double,
    var timezone: String,
    var ceiling: Double,
    var startsAt: Int,
    var status: BatchedAuditMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAuditMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAuditMilestoneStatus): Boolean {
        when (status) {
            BatchedAuditMilestoneStatus.DRAFT -> {
                when (next) {
                    BatchedAuditMilestoneStatus.PENDING, BatchedAuditMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            BatchedAuditMilestoneStatus.PENDING -> {
                when (next) {
                    BatchedAuditMilestoneStatus.ACTIVE, BatchedAuditMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            BatchedAuditMilestoneStatus.ACTIVE -> return next == BatchedAuditMilestoneStatus.SUSPENDED
            BatchedAuditMilestoneStatus.SUSPENDED -> {
                when (next) {
                    BatchedAuditMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAuditMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched audit milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round(pallets.toDouble() * loadKg * 100) / 100

    fun snapshot(): BatchedAuditMilestone {
        return BatchedAuditMilestone(
            code = code,
            title = title,
            minimum = minimum,
            segment = segment,
            pallets = pallets,
            contactEmail = contactEmail,
            loadKg = loadKg,
            timezone = timezone,
            ceiling = ceiling,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched audit milestone in status draft. */
fun newBatchedAuditMilestone(code: String, title: String): BatchedAuditMilestone {
    return BatchedAuditMilestone(
        code = code,
        title = title,
        minimum = 0.0,
        segment = "",
        pallets = 0,
        contactEmail = "",
        loadKg = 0.0,
        timezone = "",
        ceiling = 0.0,
        startsAt = 0,
        status = BatchedAuditMilestoneStatus.DRAFT,
    )
}
