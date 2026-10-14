package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan allocation is in its life cycle. */
enum class AdaptiveLanePlanAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A adaptive lane plan allocation. Its code is unique within a tenant. */
data class AdaptiveLanePlanAllocation(
    var code: String,
    var title: String,
    var externalRef: String,
    var capacity: Int,
    var maximum: Double,
    var routeCode: String,
    var margin: Double,
    var status: AdaptiveLanePlanAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanAllocationStatus): Boolean {
        when (status) {
            AdaptiveLanePlanAllocationStatus.CREATED -> {
                when (next) {
                    AdaptiveLanePlanAllocationStatus.PUBLISHED, AdaptiveLanePlanAllocationStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveLanePlanAllocationStatus.VALIDATED -> return next == AdaptiveLanePlanAllocationStatus.RETIRED
            AdaptiveLanePlanAllocationStatus.PUBLISHED -> return next == AdaptiveLanePlanAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanAllocationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round(margin * maximum * 100) / 100

    fun snapshot(): AdaptiveLanePlanAllocation {
        return AdaptiveLanePlanAllocation(
            code = code,
            title = title,
            externalRef = externalRef,
            capacity = capacity,
            maximum = maximum,
            routeCode = routeCode,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive lane plan allocation in status created. */
fun newAdaptiveLanePlanAllocation(code: String, title: String): AdaptiveLanePlanAllocation {
    return AdaptiveLanePlanAllocation(
        code = code,
        title = title,
        externalRef = "",
        capacity = 0,
        maximum = 0.0,
        routeCode = "",
        margin = 0.0,
        status = AdaptiveLanePlanAllocationStatus.CREATED,
    )
}
