package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional defect allocation is in its life cycle. */
enum class RegionalDefectAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional defect allocation and its current status. */
data class RegionalDefectAllocation(
    var code: String,
    var name: String,
    var reviewer: String,
    var carrier: String,
    var country: String,
    var checksum: String,
    var revision: Int,
    var odometer: Int,
    var source: String,
    var status: RegionalDefectAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectAllocationStatus): Boolean {
        when (status) {
            RegionalDefectAllocationStatus.PROPOSED -> return next == RegionalDefectAllocationStatus.APPROVED || next == RegionalDefectAllocationStatus.REJECTED
            RegionalDefectAllocationStatus.APPROVED -> return next == RegionalDefectAllocationStatus.REJECTED || next == RegionalDefectAllocationStatus.WITHDRAWN
            RegionalDefectAllocationStatus.REJECTED -> {
                when (next) {
                    RegionalDefectAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDefectAllocationStatus) {
        if (!canMoveTo(next)) {
            error("regional defect allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round(revision.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): RegionalDefectAllocation {
        return RegionalDefectAllocation(
            code = code,
            name = name,
            reviewer = reviewer,
            carrier = carrier,
            country = country,
            checksum = checksum,
            revision = revision,
            odometer = odometer,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional defect allocation in status proposed. */
fun newRegionalDefectAllocation(code: String, name: String): RegionalDefectAllocation {
    return RegionalDefectAllocation(
        code = code,
        name = name,
        reviewer = "",
        carrier = "",
        country = "",
        checksum = "",
        revision = 0,
        odometer = 0,
        source = "",
        status = RegionalDefectAllocationStatus.PROPOSED,
    )
}
