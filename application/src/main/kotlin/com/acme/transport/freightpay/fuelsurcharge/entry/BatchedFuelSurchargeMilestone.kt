package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched fuel surcharge milestone is in its life cycle. */
enum class BatchedFuelSurchargeMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched fuel surcharge milestone as the fuel surcharge team keeps it. */
data class BatchedFuelSurchargeMilestone(
    var code: String,
    var name: String,
    var title: String,
    var trailerId: String,
    var source: String,
    var discount: Double,
    var externalRef: String,
    var weight: Double,
    var status: BatchedFuelSurchargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelSurchargeMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelSurchargeMilestoneStatus): Boolean {
        when (status) {
            BatchedFuelSurchargeMilestoneStatus.IDLE -> {
                when (next) {
                    BatchedFuelSurchargeMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            BatchedFuelSurchargeMilestoneStatus.WARMING -> return next == BatchedFuelSurchargeMilestoneStatus.READY
            BatchedFuelSurchargeMilestoneStatus.READY -> {
                when (next) {
                    BatchedFuelSurchargeMilestoneStatus.DRAINING, BatchedFuelSurchargeMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            BatchedFuelSurchargeMilestoneStatus.DRAINING -> return next == BatchedFuelSurchargeMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedFuelSurchargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel surcharge milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(weight * discount * 100) / 100

    fun snapshot(): BatchedFuelSurchargeMilestone {
        return BatchedFuelSurchargeMilestone(
            code = code,
            name = name,
            title = title,
            trailerId = trailerId,
            source = source,
            discount = discount,
            externalRef = externalRef,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched fuel surcharge milestone in status idle. */
fun newBatchedFuelSurchargeMilestone(code: String, name: String): BatchedFuelSurchargeMilestone {
    return BatchedFuelSurchargeMilestone(
        code = code,
        name = name,
        title = "",
        trailerId = "",
        source = "",
        discount = 0.0,
        externalRef = "",
        weight = 0.0,
        status = BatchedFuelSurchargeMilestoneStatus.IDLE,
    )
}
