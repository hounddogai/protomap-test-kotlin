package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute allocation is in its life cycle. */
enum class DisputeAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dispute allocation as the dispute team keeps it. */
data class DisputeAllocation(
    var code: String,
    var name: String,
    var windowDays: Int,
    var summary: String,
    var active: Boolean,
    var rate: Double,
    var ceiling: Double,
    var stopCount: Int,
    var archived: Boolean,
    var maximum: Double,
    var revision: Int,
    var status: DisputeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeAllocationStatus): Boolean {
        when (status) {
            DisputeAllocationStatus.OPEN -> {
                when (next) {
                    DisputeAllocationStatus.RESOLVED -> return true
                }
                return false
            }
            DisputeAllocationStatus.IN_REVIEW -> {
                when (next) {
                    DisputeAllocationStatus.RESOLVED -> return true
                }
                return false
            }
            DisputeAllocationStatus.RESOLVED -> return next == DisputeAllocationStatus.REOPENED || next == DisputeAllocationStatus.ARCHIVED
            DisputeAllocationStatus.REOPENED -> {
                when (next) {
                    DisputeAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("dispute allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((rate - revision.toDouble()) * 100) / 100

    fun snapshot(): DisputeAllocation {
        return DisputeAllocation(
            code = code,
            name = name,
            windowDays = windowDays,
            summary = summary,
            active = active,
            rate = rate,
            ceiling = ceiling,
            stopCount = stopCount,
            archived = archived,
            maximum = maximum,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute allocation in status open. */
fun newDisputeAllocation(code: String, name: String): DisputeAllocation {
    return DisputeAllocation(
        code = code,
        name = name,
        windowDays = 0,
        summary = "",
        active = false,
        rate = 0.0,
        ceiling = 0.0,
        stopCount = 0,
        archived = false,
        maximum = 0.0,
        revision = 0,
        status = DisputeAllocationStatus.OPEN,
    )
}
