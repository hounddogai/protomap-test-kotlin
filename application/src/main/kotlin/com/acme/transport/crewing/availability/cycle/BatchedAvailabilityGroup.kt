package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize

/** Where a batched availability group is in its life cycle. */
enum class BatchedAvailabilityGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched availability group, with what the planners need to act on it. */
data class BatchedAvailabilityGroup(
    var code: String,
    var label: String,
    var ownerId: String,
    var odometer: Int,
    var checksum: String,
    var carrier: String,
    var status: BatchedAvailabilityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAvailabilityGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAvailabilityGroupStatus): Boolean {
        when (status) {
            BatchedAvailabilityGroupStatus.DRAFT -> {
                when (next) {
                    BatchedAvailabilityGroupStatus.PENDING, BatchedAvailabilityGroupStatus.CLOSED -> return true
                }
                return false
            }
            BatchedAvailabilityGroupStatus.PENDING -> return next == BatchedAvailabilityGroupStatus.ACTIVE || next == BatchedAvailabilityGroupStatus.SUSPENDED
            BatchedAvailabilityGroupStatus.ACTIVE -> {
                when (next) {
                    BatchedAvailabilityGroupStatus.SUSPENDED, BatchedAvailabilityGroupStatus.CLOSED -> return true
                }
                return false
            }
            BatchedAvailabilityGroupStatus.SUSPENDED -> {
                when (next) {
                    BatchedAvailabilityGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAvailabilityGroupStatus) {
        if (!canMoveTo(next)) {
            error("batched availability group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): BatchedAvailabilityGroup {
        return BatchedAvailabilityGroup(
            code = code,
            label = label,
            ownerId = ownerId,
            odometer = odometer,
            checksum = checksum,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched availability group in status draft. */
fun newBatchedAvailabilityGroup(code: String, label: String): BatchedAvailabilityGroup {
    return BatchedAvailabilityGroup(
        code = code,
        label = label,
        ownerId = "",
        odometer = 0,
        checksum = "",
        carrier = "",
        status = BatchedAvailabilityGroupStatus.DRAFT,
    )
}
