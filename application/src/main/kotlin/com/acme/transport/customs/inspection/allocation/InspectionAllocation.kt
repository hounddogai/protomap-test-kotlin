package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection allocation records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection allocation is in its life cycle. */
enum class InspectionAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection allocation as the inspection team keeps it. */
data class InspectionAllocation(
    var code: String,
    var name: String,
    var currency: String,
    var priority: Int,
    var dock: String,
    var ratio: Double,
    var retries: Int,
    var region: String,
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
            InspectionAllocationStatus.DRAFT -> {
                when (next) {
                    InspectionAllocationStatus.PENDING, InspectionAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionAllocationStatus.PENDING -> return next == InspectionAllocationStatus.SUSPENDED
            InspectionAllocationStatus.ACTIVE -> return next == InspectionAllocationStatus.SUSPENDED
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
    }

    fun label(): String = "${"inspection allocation"} $code"

    fun weightedTotal(): Double = round((ratio + priority.toDouble()) * 100) / 100

    fun snapshot(): InspectionAllocation {
        return InspectionAllocation(
            code = code,
            name = name,
            currency = currency,
            priority = priority,
            dock = dock,
            ratio = ratio,
            retries = retries,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a inspection allocation in status draft. */
fun newInspectionAllocation(code: String, name: String): InspectionAllocation {
    return InspectionAllocation(
        code = code,
        name = name,
        currency = "",
        priority = 0,
        dock = "",
        ratio = 0.0,
        retries = 0,
        region = "",
        status = InspectionAllocationStatus.DRAFT,
    )
}
