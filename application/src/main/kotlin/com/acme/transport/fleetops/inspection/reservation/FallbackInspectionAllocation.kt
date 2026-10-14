package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection allocation is in its life cycle. */
enum class FallbackInspectionAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback inspection allocation as the inspection team keeps it. */
data class FallbackInspectionAllocation(
    var code: String,
    var name: String,
    var shiftCode: String,
    var maximum: Double,
    var limit: Int,
    var status: FallbackInspectionAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionAllocationStatus): Boolean {
        when (status) {
            FallbackInspectionAllocationStatus.REQUESTED -> return next == FallbackInspectionAllocationStatus.GRANTED || next == FallbackInspectionAllocationStatus.EXPIRED
            FallbackInspectionAllocationStatus.GRANTED -> {
                when (next) {
                    FallbackInspectionAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackInspectionAllocationStatus.REVOKED -> {
                when (next) {
                    FallbackInspectionAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackInspectionAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((maximum + limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackInspectionAllocation {
        return FallbackInspectionAllocation(
            code = code,
            name = name,
            shiftCode = shiftCode,
            maximum = maximum,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback inspection allocation in status requested. */
fun newFallbackInspectionAllocation(code: String, name: String): FallbackInspectionAllocation {
    return FallbackInspectionAllocation(
        code = code,
        name = name,
        shiftCode = "",
        maximum = 0.0,
        limit = 0,
        status = FallbackInspectionAllocationStatus.REQUESTED,
    )
}
