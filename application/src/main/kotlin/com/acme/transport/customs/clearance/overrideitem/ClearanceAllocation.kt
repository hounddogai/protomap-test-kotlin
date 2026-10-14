package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance allocation is in its life cycle. */
enum class ClearanceAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A clearance allocation as the clearance team keeps it. */
data class ClearanceAllocation(
    var code: String,
    var label: String,
    var limit: Int,
    var currency: String,
    var externalRef: String,
    var stopCount: Int,
    var margin: Double,
    var expiresAt: Int,
    var cost: Double,
    var name: String,
    var status: ClearanceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceAllocationStatus): Boolean {
        when (status) {
            ClearanceAllocationStatus.OPEN -> {
                when (next) {
                    ClearanceAllocationStatus.IN_REVIEW, ClearanceAllocationStatus.REOPENED -> return true
                }
                return false
            }
            ClearanceAllocationStatus.IN_REVIEW -> return next == ClearanceAllocationStatus.RESOLVED || next == ClearanceAllocationStatus.ARCHIVED
            ClearanceAllocationStatus.RESOLVED -> {
                when (next) {
                    ClearanceAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            ClearanceAllocationStatus.REOPENED -> return next == ClearanceAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ClearanceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("clearance allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((expiresAt.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): ClearanceAllocation {
        return ClearanceAllocation(
            code = code,
            label = label,
            limit = limit,
            currency = currency,
            externalRef = externalRef,
            stopCount = stopCount,
            margin = margin,
            expiresAt = expiresAt,
            cost = cost,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance allocation in status open. */
fun newClearanceAllocation(code: String, label: String): ClearanceAllocation {
    return ClearanceAllocation(
        code = code,
        label = label,
        limit = 0,
        currency = "",
        externalRef = "",
        stopCount = 0,
        margin = 0.0,
        expiresAt = 0,
        cost = 0.0,
        name = "",
        status = ClearanceAllocationStatus.OPEN,
    )
}
