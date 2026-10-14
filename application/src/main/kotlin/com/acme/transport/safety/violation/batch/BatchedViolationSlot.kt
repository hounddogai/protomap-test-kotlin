package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize

/** Where a batched violation slot is in its life cycle. */
enum class BatchedViolationSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched violation slot. Its code is unique within a tenant. */
data class BatchedViolationSlot(
    var code: String,
    var label: String,
    var name: String,
    var visible: Boolean,
    var vehicleId: String,
    var amount: Double,
    var status: BatchedViolationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedViolationSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedViolationSlotStatus): Boolean {
        when (status) {
            BatchedViolationSlotStatus.OPEN -> return next == BatchedViolationSlotStatus.RESOLVED || next == BatchedViolationSlotStatus.REOPENED
            BatchedViolationSlotStatus.IN_REVIEW -> {
                when (next) {
                    BatchedViolationSlotStatus.REOPENED -> return true
                }
                return false
            }
            BatchedViolationSlotStatus.RESOLVED -> return next == BatchedViolationSlotStatus.ARCHIVED
            BatchedViolationSlotStatus.REOPENED -> {
                when (next) {
                    BatchedViolationSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedViolationSlotStatus) {
        if (!canMoveTo(next)) {
            error("batched violation slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched violation slot"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): BatchedViolationSlot {
        return BatchedViolationSlot(
            code = code,
            label = label,
            name = name,
            visible = visible,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched violation slot in status open. */
fun newBatchedViolationSlot(code: String, label: String): BatchedViolationSlot {
    return BatchedViolationSlot(
        code = code,
        label = label,
        name = "",
        visible = false,
        vehicleId = "",
        amount = 0.0,
        status = BatchedViolationSlotStatus.OPEN,
    )
}
