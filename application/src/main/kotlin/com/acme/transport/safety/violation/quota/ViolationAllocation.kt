package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.normalize

/** Where a violation allocation is in its life cycle. */
enum class ViolationAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a violation allocation and its current status. */
data class ViolationAllocation(
    var code: String,
    var label: String,
    var createdAt: Int,
    var segment: String,
    var visible: Boolean,
    var status: ViolationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationAllocationStatus): Boolean {
        when (status) {
            ViolationAllocationStatus.CREATED -> {
                when (next) {
                    ViolationAllocationStatus.VALIDATED -> return true
                }
                return false
            }
            ViolationAllocationStatus.VALIDATED -> return next == ViolationAllocationStatus.PUBLISHED || next == ViolationAllocationStatus.RETIRED
            ViolationAllocationStatus.PUBLISHED -> return next == ViolationAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ViolationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("violation allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"violation allocation"} $code"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): ViolationAllocation {
        return ViolationAllocation(
            code = code,
            label = label,
            createdAt = createdAt,
            segment = segment,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a violation allocation in status created. */
fun newViolationAllocation(code: String, label: String): ViolationAllocation {
    return ViolationAllocation(
        code = code,
        label = label,
        createdAt = 0,
        segment = "",
        visible = false,
        status = ViolationAllocationStatus.CREATED,
    )
}
