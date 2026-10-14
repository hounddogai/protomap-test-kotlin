package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched utilization budget is in its life cycle. */
enum class BatchedUtilizationBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched utilization budget. Its code is unique within a tenant. */
data class BatchedUtilizationBudget(
    var code: String,
    var name: String,
    var retries: Int,
    var shiftCode: String,
    var distanceKm: Double,
    var channel: String,
    var version: Int,
    var status: BatchedUtilizationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedUtilizationBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedUtilizationBudgetStatus): Boolean {
        when (status) {
            BatchedUtilizationBudgetStatus.OPEN -> return next == BatchedUtilizationBudgetStatus.REOPENED || next == BatchedUtilizationBudgetStatus.ARCHIVED
            BatchedUtilizationBudgetStatus.IN_REVIEW -> return next == BatchedUtilizationBudgetStatus.REOPENED || next == BatchedUtilizationBudgetStatus.ARCHIVED
            BatchedUtilizationBudgetStatus.RESOLVED -> return next == BatchedUtilizationBudgetStatus.ARCHIVED
            BatchedUtilizationBudgetStatus.REOPENED -> {
                when (next) {
                    BatchedUtilizationBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedUtilizationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched utilization budget $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(version.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): BatchedUtilizationBudget {
        return BatchedUtilizationBudget(
            code = code,
            name = name,
            retries = retries,
            shiftCode = shiftCode,
            distanceKm = distanceKm,
            channel = channel,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched utilization budget in status open. */
fun newBatchedUtilizationBudget(code: String, name: String): BatchedUtilizationBudget {
    return BatchedUtilizationBudget(
        code = code,
        name = name,
        retries = 0,
        shiftCode = "",
        distanceKm = 0.0,
        channel = "",
        version = 0,
        status = BatchedUtilizationBudgetStatus.OPEN,
    )
}
