package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict emission factor allocation is in its life cycle. */
enum class StrictEmissionFactorAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict emission factor allocation and its current status. */
data class StrictEmissionFactorAllocation(
    var code: String,
    var title: String,
    var name: String,
    var pallets: Int,
    var timezone: String,
    var driverId: String,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var status: StrictEmissionFactorAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEmissionFactorAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEmissionFactorAllocationStatus): Boolean {
        when (status) {
            StrictEmissionFactorAllocationStatus.IDLE -> {
                when (next) {
                    StrictEmissionFactorAllocationStatus.STOPPED -> return true
                }
                return false
            }
            StrictEmissionFactorAllocationStatus.WARMING -> {
                when (next) {
                    StrictEmissionFactorAllocationStatus.READY, StrictEmissionFactorAllocationStatus.DRAINING -> return true
                }
                return false
            }
            StrictEmissionFactorAllocationStatus.READY -> return next == StrictEmissionFactorAllocationStatus.STOPPED
            StrictEmissionFactorAllocationStatus.DRAINING -> return next == StrictEmissionFactorAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictEmissionFactorAllocationStatus) {
        if (!canMoveTo(next)) {
            error("strict emission factor allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict emission factor allocation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((pallets.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): StrictEmissionFactorAllocation {
        return StrictEmissionFactorAllocation(
            code = code,
            title = title,
            name = name,
            pallets = pallets,
            timezone = timezone,
            driverId = driverId,
            durationMinutes = durationMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict emission factor allocation in status idle. */
fun newStrictEmissionFactorAllocation(code: String, title: String): StrictEmissionFactorAllocation {
    return StrictEmissionFactorAllocation(
        code = code,
        title = title,
        name = "",
        pallets = 0,
        timezone = "",
        driverId = "",
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        status = StrictEmissionFactorAllocationStatus.IDLE,
    )
}
