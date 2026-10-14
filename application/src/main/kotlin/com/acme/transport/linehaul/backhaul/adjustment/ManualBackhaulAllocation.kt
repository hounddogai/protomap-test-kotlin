package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual backhaul allocation is in its life cycle. */
enum class ManualBackhaulAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual backhaul allocation. Its code is unique within a tenant. */
data class ManualBackhaulAllocation(
    var code: String,
    var name: String,
    var startsAt: Int,
    var summary: String,
    var vehicleId: String,
    var expiresAt: Int,
    var status: ManualBackhaulAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulAllocationStatus): Boolean {
        when (status) {
            ManualBackhaulAllocationStatus.REQUESTED -> {
                when (next) {
                    ManualBackhaulAllocationStatus.GRANTED, ManualBackhaulAllocationStatus.REVOKED -> return true
                }
                return false
            }
            ManualBackhaulAllocationStatus.GRANTED -> return next == ManualBackhaulAllocationStatus.REVOKED || next == ManualBackhaulAllocationStatus.EXPIRED
            ManualBackhaulAllocationStatus.REVOKED -> {
                when (next) {
                    ManualBackhaulAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualBackhaulAllocationStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((startsAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ManualBackhaulAllocation {
        return ManualBackhaulAllocation(
            code = code,
            name = name,
            startsAt = startsAt,
            summary = summary,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual backhaul allocation in status requested. */
fun newManualBackhaulAllocation(code: String, name: String): ManualBackhaulAllocation {
    return ManualBackhaulAllocation(
        code = code,
        name = name,
        startsAt = 0,
        summary = "",
        vehicleId = "",
        expiresAt = 0,
        status = ManualBackhaulAllocationStatus.REQUESTED,
    )
}
