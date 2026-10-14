package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global violation allocation is in its life cycle. */
enum class GlobalViolationAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global violation allocation and its current status. */
data class GlobalViolationAllocation(
    var code: String,
    var title: String,
    var weight: Double,
    var version: Int,
    var batchSize: Int,
    var segment: String,
    var status: GlobalViolationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationAllocationStatus): Boolean {
        when (status) {
            GlobalViolationAllocationStatus.PLANNED -> {
                when (next) {
                    GlobalViolationAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalViolationAllocationStatus.SCHEDULED -> return next == GlobalViolationAllocationStatus.IN_PROGRESS
            GlobalViolationAllocationStatus.IN_PROGRESS -> return next == GlobalViolationAllocationStatus.CANCELLED
            GlobalViolationAllocationStatus.COMPLETED -> return next == GlobalViolationAllocationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalViolationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global violation allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"global violation allocation"} $code"

    fun effectiveVolume(): Double = round(version.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): GlobalViolationAllocation {
        return GlobalViolationAllocation(
            code = code,
            title = title,
            weight = weight,
            version = version,
            batchSize = batchSize,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global violation allocation in status planned. */
fun newGlobalViolationAllocation(code: String, title: String): GlobalViolationAllocation {
    return GlobalViolationAllocation(
        code = code,
        title = title,
        weight = 0.0,
        version = 0,
        batchSize = 0,
        segment = "",
        status = GlobalViolationAllocationStatus.PLANNED,
    )
}
