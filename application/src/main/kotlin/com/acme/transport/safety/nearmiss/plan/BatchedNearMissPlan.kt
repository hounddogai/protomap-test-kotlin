package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize

/** Where a batched near miss plan is in its life cycle. */
enum class BatchedNearMissPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched near miss plan as the near miss team keeps it. */
data class BatchedNearMissPlan(
    var code: String,
    var name: String,
    var country: String,
    var vehicleId: String,
    var region: String,
    var visible: Boolean,
    var approved: Boolean,
    var amount: Double,
    var status: BatchedNearMissPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNearMissPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNearMissPlanStatus): Boolean {
        when (status) {
            BatchedNearMissPlanStatus.OPEN -> {
                when (next) {
                    BatchedNearMissPlanStatus.REOPENED, BatchedNearMissPlanStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedNearMissPlanStatus.IN_REVIEW -> return next == BatchedNearMissPlanStatus.ARCHIVED
            BatchedNearMissPlanStatus.RESOLVED -> {
                when (next) {
                    BatchedNearMissPlanStatus.REOPENED -> return true
                }
                return false
            }
            BatchedNearMissPlanStatus.REOPENED -> {
                when (next) {
                    BatchedNearMissPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNearMissPlanStatus) {
        if (!canMoveTo(next)) {
            error("batched near miss plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): BatchedNearMissPlan {
        return BatchedNearMissPlan(
            code = code,
            name = name,
            country = country,
            vehicleId = vehicleId,
            region = region,
            visible = visible,
            approved = approved,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched near miss plan in status open. */
fun newBatchedNearMissPlan(code: String, name: String): BatchedNearMissPlan {
    return BatchedNearMissPlan(
        code = code,
        name = name,
        country = "",
        vehicleId = "",
        region = "",
        visible = false,
        approved = false,
        amount = 0.0,
        status = BatchedNearMissPlanStatus.OPEN,
    )
}
