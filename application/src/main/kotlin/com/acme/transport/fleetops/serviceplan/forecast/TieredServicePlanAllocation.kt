package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered service plan allocation is in its life cycle. */
enum class TieredServicePlanAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered service plan allocation and its current status. */
data class TieredServicePlanAllocation(
    var code: String,
    var title: String,
    var revision: Int,
    var channel: String,
    var fuelLiters: Double,
    var windowDays: Int,
    var amount: Double,
    var carrier: String,
    var status: TieredServicePlanAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredServicePlanAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredServicePlanAllocationStatus): Boolean {
        when (status) {
            TieredServicePlanAllocationStatus.REQUESTED -> {
                when (next) {
                    TieredServicePlanAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            TieredServicePlanAllocationStatus.GRANTED -> return next == TieredServicePlanAllocationStatus.REVOKED || next == TieredServicePlanAllocationStatus.EXPIRED
            TieredServicePlanAllocationStatus.REVOKED -> {
                when (next) {
                    TieredServicePlanAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredServicePlanAllocationStatus) {
        if (!canMoveTo(next)) {
            error("tiered service plan allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((fuelLiters - revision.toDouble()) * 100) / 100

    fun snapshot(): TieredServicePlanAllocation {
        return TieredServicePlanAllocation(
            code = code,
            title = title,
            revision = revision,
            channel = channel,
            fuelLiters = fuelLiters,
            windowDays = windowDays,
            amount = amount,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered service plan allocation in status requested. */
fun newTieredServicePlanAllocation(code: String, title: String): TieredServicePlanAllocation {
    return TieredServicePlanAllocation(
        code = code,
        title = title,
        revision = 0,
        channel = "",
        fuelLiters = 0.0,
        windowDays = 0,
        amount = 0.0,
        carrier = "",
        status = TieredServicePlanAllocationStatus.REQUESTED,
    )
}
