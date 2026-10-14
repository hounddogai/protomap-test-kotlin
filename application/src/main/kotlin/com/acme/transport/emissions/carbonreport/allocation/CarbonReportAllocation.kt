package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report allocation records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report allocation is in its life cycle. */
enum class CarbonReportAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a carbon report allocation and its current status. */
data class CarbonReportAllocation(
    var code: String,
    var name: String,
    var label: String,
    var expiresAt: Int,
    var priority: Int,
    var status: CarbonReportAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportAllocationStatus): Boolean {
        when (status) {
            CarbonReportAllocationStatus.IDLE -> return next == CarbonReportAllocationStatus.WARMING
            CarbonReportAllocationStatus.WARMING -> return next == CarbonReportAllocationStatus.DRAINING || next == CarbonReportAllocationStatus.STOPPED
            CarbonReportAllocationStatus.READY -> return next == CarbonReportAllocationStatus.DRAINING || next == CarbonReportAllocationStatus.STOPPED
            CarbonReportAllocationStatus.DRAINING -> return next == CarbonReportAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CarbonReportAllocationStatus) {
        if (!canMoveTo(next)) {
            error("carbon report allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"carbon report allocation"} $code"

    fun adjustedLoad(): Double = round(priority.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): CarbonReportAllocation {
        return CarbonReportAllocation(
            code = code,
            name = name,
            label = label,
            expiresAt = expiresAt,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a carbon report allocation in status idle. */
fun newCarbonReportAllocation(code: String, name: String): CarbonReportAllocation {
    return CarbonReportAllocation(
        code = code,
        name = name,
        label = "",
        expiresAt = 0,
        priority = 0,
        status = CarbonReportAllocationStatus.IDLE,
    )
}
