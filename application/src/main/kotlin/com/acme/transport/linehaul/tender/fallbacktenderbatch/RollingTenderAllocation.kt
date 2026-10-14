package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tender allocation is in its life cycle. */
enum class RollingTenderAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling tender allocation. Its code is unique within a tenant. */
data class RollingTenderAllocation(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var ratio: Double,
    var status: RollingTenderAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTenderAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTenderAllocationStatus): Boolean {
        when (status) {
            RollingTenderAllocationStatus.DRAFT -> {
                when (next) {
                    RollingTenderAllocationStatus.ACTIVE, RollingTenderAllocationStatus.CLOSED -> return true
                }
                return false
            }
            RollingTenderAllocationStatus.PENDING -> return next == RollingTenderAllocationStatus.SUSPENDED || next == RollingTenderAllocationStatus.CLOSED
            RollingTenderAllocationStatus.ACTIVE -> return next == RollingTenderAllocationStatus.SUSPENDED
            RollingTenderAllocationStatus.SUSPENDED -> return next == RollingTenderAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingTenderAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling tender allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round(ratio * fuelLiters * 100) / 100

    fun snapshot(): RollingTenderAllocation {
        return RollingTenderAllocation(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            tags = tags,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling tender allocation in status draft. */
fun newRollingTenderAllocation(code: String, label: String): RollingTenderAllocation {
    return RollingTenderAllocation(
        code = code,
        label = label,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        ratio = 0.0,
        status = RollingTenderAllocationStatus.DRAFT,
    )
}
