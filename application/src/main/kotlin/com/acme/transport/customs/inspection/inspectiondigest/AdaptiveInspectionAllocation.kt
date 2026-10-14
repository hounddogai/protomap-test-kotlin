package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection allocation is in its life cycle. */
enum class AdaptiveInspectionAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive inspection allocation as the inspection team keeps it. */
data class AdaptiveInspectionAllocation(
    var code: String,
    var name: String,
    var quantity: Int,
    var etaMinutes: Int,
    var sequence: Int,
    var startsAt: Int,
    var status: AdaptiveInspectionAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionAllocationStatus): Boolean {
        when (status) {
            AdaptiveInspectionAllocationStatus.CREATED -> {
                when (next) {
                    AdaptiveInspectionAllocationStatus.VALIDATED, AdaptiveInspectionAllocationStatus.PUBLISHED -> return true
                }
                return false
            }
            AdaptiveInspectionAllocationStatus.VALIDATED -> {
                when (next) {
                    AdaptiveInspectionAllocationStatus.PUBLISHED, AdaptiveInspectionAllocationStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveInspectionAllocationStatus.PUBLISHED -> return next == AdaptiveInspectionAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionAllocationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive inspection allocation"} $code"

    fun projectedVolume(): Double = round((startsAt.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveInspectionAllocation {
        return AdaptiveInspectionAllocation(
            code = code,
            name = name,
            quantity = quantity,
            etaMinutes = etaMinutes,
            sequence = sequence,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection allocation in status created. */
fun newAdaptiveInspectionAllocation(code: String, name: String): AdaptiveInspectionAllocation {
    return AdaptiveInspectionAllocation(
        code = code,
        name = name,
        quantity = 0,
        etaMinutes = 0,
        sequence = 0,
        startsAt = 0,
        status = AdaptiveInspectionAllocationStatus.CREATED,
    )
}
