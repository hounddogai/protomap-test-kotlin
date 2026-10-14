package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn allocation is in its life cycle. */
enum class FuelBurnAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fuel burn allocation. Its code is unique within a tenant. */
data class FuelBurnAllocation(
    var code: String,
    var name: String,
    var limit: Int,
    var summary: String,
    var active: Boolean,
    var label: String,
    var batchSize: Int,
    var windowDays: Int,
    var retries: Int,
    var status: FuelBurnAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnAllocationStatus): Boolean {
        when (status) {
            FuelBurnAllocationStatus.IDLE -> return next == FuelBurnAllocationStatus.WARMING || next == FuelBurnAllocationStatus.READY
            FuelBurnAllocationStatus.WARMING -> return next == FuelBurnAllocationStatus.READY
            FuelBurnAllocationStatus.READY -> return next == FuelBurnAllocationStatus.STOPPED
            FuelBurnAllocationStatus.DRAINING -> {
                when (next) {
                    FuelBurnAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun projectedValue(): Double = round((batchSize.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnAllocation {
        return FuelBurnAllocation(
            code = code,
            name = name,
            limit = limit,
            summary = summary,
            active = active,
            label = label,
            batchSize = batchSize,
            windowDays = windowDays,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel burn allocation in status idle. */
fun newFuelBurnAllocation(code: String, name: String): FuelBurnAllocation {
    return FuelBurnAllocation(
        code = code,
        name = name,
        limit = 0,
        summary = "",
        active = false,
        label = "",
        batchSize = 0,
        windowDays = 0,
        retries = 0,
        status = FuelBurnAllocationStatus.IDLE,
    )
}
