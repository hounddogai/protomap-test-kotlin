package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect allocation records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a defect allocation is in its life cycle. */
enum class DefectAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A defect allocation. Its code is unique within a tenant. */
data class DefectAllocation(
    var code: String,
    var label: String,
    var channel: String,
    var stopCount: Int,
    var routeCode: String,
    var tags: MutableList<String>,
    var shiftCode: String,
    var startsAt: Int,
    var status: DefectAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectAllocationStatus): Boolean {
        when (status) {
            DefectAllocationStatus.DRAFT -> return next == DefectAllocationStatus.PENDING || next == DefectAllocationStatus.SUSPENDED
            DefectAllocationStatus.PENDING -> return next == DefectAllocationStatus.SUSPENDED
            DefectAllocationStatus.ACTIVE -> {
                when (next) {
                    DefectAllocationStatus.SUSPENDED, DefectAllocationStatus.CLOSED -> return true
                }
                return false
            }
            DefectAllocationStatus.SUSPENDED -> {
                when (next) {
                    DefectAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectAllocationStatus) {
        if (!canMoveTo(next)) {
            error("defect allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((startsAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DefectAllocation {
        return DefectAllocation(
            code = code,
            label = label,
            channel = channel,
            stopCount = stopCount,
            routeCode = routeCode,
            tags = tags,
            shiftCode = shiftCode,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a defect allocation in status draft. */
fun newDefectAllocation(code: String, label: String): DefectAllocation {
    return DefectAllocation(
        code = code,
        label = label,
        channel = "",
        stopCount = 0,
        routeCode = "",
        tags = mutableListOf<String>(),
        shiftCode = "",
        startsAt = 0,
        status = DefectAllocationStatus.DRAFT,
    )
}
