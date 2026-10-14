package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service plan target is in its life cycle. */
enum class BatchedServicePlanTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a batched service plan target and its current status. */
data class BatchedServicePlanTarget(
    var code: String,
    var name: String,
    var weight: Double,
    var updatedAt: Int,
    var attempts: Int,
    var status: BatchedServicePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServicePlanTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServicePlanTargetStatus): Boolean {
        when (status) {
            BatchedServicePlanTargetStatus.IDLE -> return next == BatchedServicePlanTargetStatus.READY
            BatchedServicePlanTargetStatus.WARMING -> return next == BatchedServicePlanTargetStatus.READY
            BatchedServicePlanTargetStatus.READY -> return next == BatchedServicePlanTargetStatus.STOPPED
            BatchedServicePlanTargetStatus.DRAINING -> return next == BatchedServicePlanTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedServicePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("batched service plan target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round(attempts.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): BatchedServicePlanTarget {
        return BatchedServicePlanTarget(
            code = code,
            name = name,
            weight = weight,
            updatedAt = updatedAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched service plan target in status idle. */
fun newBatchedServicePlanTarget(code: String, name: String): BatchedServicePlanTarget {
    return BatchedServicePlanTarget(
        code = code,
        name = name,
        weight = 0.0,
        updatedAt = 0,
        attempts = 0,
        status = BatchedServicePlanTargetStatus.IDLE,
    )
}
