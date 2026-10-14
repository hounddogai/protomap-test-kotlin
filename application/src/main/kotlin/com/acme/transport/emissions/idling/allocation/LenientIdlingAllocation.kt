package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient idling allocation records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a lenient idling allocation is in its life cycle. */
enum class LenientIdlingAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient idling allocation and its current status. */
data class LenientIdlingAllocation(
    var code: String,
    var label: String,
    var priority: Int,
    var margin: Double,
    var budget: Double,
    var region: String,
    var reviewer: String,
    var phone: String,
    var status: LenientIdlingAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIdlingAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIdlingAllocationStatus): Boolean {
        when (status) {
            LenientIdlingAllocationStatus.OPEN -> {
                when (next) {
                    LenientIdlingAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientIdlingAllocationStatus.IN_REVIEW -> return next == LenientIdlingAllocationStatus.RESOLVED || next == LenientIdlingAllocationStatus.ARCHIVED
            LenientIdlingAllocationStatus.RESOLVED -> {
                when (next) {
                    LenientIdlingAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientIdlingAllocationStatus.REOPENED -> return next == LenientIdlingAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientIdlingAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lenient idling allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round(budget * priority.toDouble() * 100) / 100

    fun snapshot(): LenientIdlingAllocation {
        return LenientIdlingAllocation(
            code = code,
            label = label,
            priority = priority,
            margin = margin,
            budget = budget,
            region = region,
            reviewer = reviewer,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient idling allocation in status open. */
fun newLenientIdlingAllocation(code: String, label: String): LenientIdlingAllocation {
    return LenientIdlingAllocation(
        code = code,
        label = label,
        priority = 0,
        margin = 0.0,
        budget = 0.0,
        region = "",
        reviewer = "",
        phone = "",
        status = LenientIdlingAllocationStatus.OPEN,
    )
}
