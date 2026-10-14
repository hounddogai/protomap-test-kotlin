package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.normalize

/** Where a manual zone allocation is in its life cycle. */
enum class ManualZoneAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual zone allocation. Its code is unique within a tenant. */
data class ManualZoneAllocation(
    var code: String,
    var name: String,
    var segment: String,
    var loadKg: Double,
    var summary: String,
    var status: ManualZoneAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualZoneAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualZoneAllocationStatus): Boolean {
        when (status) {
            ManualZoneAllocationStatus.CREATED -> {
                when (next) {
                    ManualZoneAllocationStatus.VALIDATED, ManualZoneAllocationStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualZoneAllocationStatus.VALIDATED -> return next == ManualZoneAllocationStatus.RETIRED
            ManualZoneAllocationStatus.PUBLISHED -> return next == ManualZoneAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualZoneAllocationStatus) {
        if (!canMoveTo(next)) {
            error("manual zone allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual zone allocation"} $code"

    fun measuredValue(): Double = loadKg

    fun snapshot(): ManualZoneAllocation {
        return ManualZoneAllocation(
            code = code,
            name = name,
            segment = segment,
            loadKg = loadKg,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual zone allocation in status created. */
fun newManualZoneAllocation(code: String, name: String): ManualZoneAllocation {
    return ManualZoneAllocation(
        code = code,
        name = name,
        segment = "",
        loadKg = 0.0,
        summary = "",
        status = ManualZoneAllocationStatus.CREATED,
    )
}
