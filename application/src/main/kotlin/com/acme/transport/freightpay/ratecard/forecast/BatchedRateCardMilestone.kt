package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched rate card milestone is in its life cycle. */
enum class BatchedRateCardMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched rate card milestone as the rate card team keeps it. */
data class BatchedRateCardMilestone(
    var code: String,
    var title: String,
    var priority: Int,
    var revision: Int,
    var retries: Int,
    var active: Boolean,
    var batchSize: Int,
    var updatedAt: Int,
    var ceiling: Double,
    var routeCode: String,
    var status: BatchedRateCardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRateCardMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRateCardMilestoneStatus): Boolean {
        when (status) {
            BatchedRateCardMilestoneStatus.IDLE -> {
                when (next) {
                    BatchedRateCardMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            BatchedRateCardMilestoneStatus.WARMING -> return next == BatchedRateCardMilestoneStatus.READY || next == BatchedRateCardMilestoneStatus.STOPPED
            BatchedRateCardMilestoneStatus.READY -> return next == BatchedRateCardMilestoneStatus.DRAINING || next == BatchedRateCardMilestoneStatus.STOPPED
            BatchedRateCardMilestoneStatus.DRAINING -> {
                when (next) {
                    BatchedRateCardMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRateCardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched rate card milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((updatedAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): BatchedRateCardMilestone {
        return BatchedRateCardMilestone(
            code = code,
            title = title,
            priority = priority,
            revision = revision,
            retries = retries,
            active = active,
            batchSize = batchSize,
            updatedAt = updatedAt,
            ceiling = ceiling,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched rate card milestone in status idle. */
fun newBatchedRateCardMilestone(code: String, title: String): BatchedRateCardMilestone {
    return BatchedRateCardMilestone(
        code = code,
        title = title,
        priority = 0,
        revision = 0,
        retries = 0,
        active = false,
        batchSize = 0,
        updatedAt = 0,
        ceiling = 0.0,
        routeCode = "",
        status = BatchedRateCardMilestoneStatus.IDLE,
    )
}
