package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.normalize

/** Where a pending inspection allocation is in its life cycle. */
enum class PendingInspectionAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A pending inspection allocation. Its code is unique within a tenant. */
data class PendingInspectionAllocation(
    var code: String,
    var title: String,
    var driverId: String,
    var budget: Double,
    var depot: String,
    var status: PendingInspectionAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionAllocationStatus): Boolean {
        when (status) {
            PendingInspectionAllocationStatus.IDLE -> {
                when (next) {
                    PendingInspectionAllocationStatus.WARMING, PendingInspectionAllocationStatus.STOPPED -> return true
                }
                return false
            }
            PendingInspectionAllocationStatus.WARMING -> return next == PendingInspectionAllocationStatus.READY
            PendingInspectionAllocationStatus.READY -> {
                when (next) {
                    PendingInspectionAllocationStatus.DRAINING -> return true
                }
                return false
            }
            PendingInspectionAllocationStatus.DRAINING -> {
                when (next) {
                    PendingInspectionAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionAllocationStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = budget

    fun snapshot(): PendingInspectionAllocation {
        return PendingInspectionAllocation(
            code = code,
            title = title,
            driverId = driverId,
            budget = budget,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending inspection allocation in status idle. */
fun newPendingInspectionAllocation(code: String, title: String): PendingInspectionAllocation {
    return PendingInspectionAllocation(
        code = code,
        title = title,
        driverId = "",
        budget = 0.0,
        depot = "",
        status = PendingInspectionAllocationStatus.IDLE,
    )
}
