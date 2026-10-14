package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection allocation is in its life cycle. */
enum class InspectionAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a inspection allocation and its current status. */
data class InspectionAllocation(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var channel: String,
    var segment: String,
    var floorValue: Double,
    var visible: Boolean,
    var status: InspectionAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAllocationStatus): Boolean {
        when (status) {
            InspectionAllocationStatus.DRAFT -> return next == InspectionAllocationStatus.ACTIVE
            InspectionAllocationStatus.PENDING -> return next == InspectionAllocationStatus.ACTIVE
            InspectionAllocationStatus.ACTIVE -> return next == InspectionAllocationStatus.CLOSED
            InspectionAllocationStatus.SUSPENDED -> {
                when (next) {
                    InspectionAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionAllocationStatus) {
        if (!canMoveTo(next)) {
            error("inspection allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((expiresAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): InspectionAllocation {
        return InspectionAllocation(
            code = code,
            label = label,
            expiresAt = expiresAt,
            channel = channel,
            segment = segment,
            floorValue = floorValue,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a inspection allocation in status draft. */
fun newInspectionAllocation(code: String, label: String): InspectionAllocation {
    return InspectionAllocation(
        code = code,
        label = label,
        expiresAt = 0,
        channel = "",
        segment = "",
        floorValue = 0.0,
        visible = false,
        status = InspectionAllocationStatus.DRAFT,
    )
}
