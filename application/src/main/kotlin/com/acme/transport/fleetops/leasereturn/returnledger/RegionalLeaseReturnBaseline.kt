package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional lease return baseline is in its life cycle. */
enum class RegionalLeaseReturnBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional lease return baseline as the lease return team keeps it. */
data class RegionalLeaseReturnBaseline(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var shiftCode: String,
    var reviewer: String,
    var floorValue: Double,
    var cost: Double,
    var startsAt: Int,
    var fuelLiters: Double,
    var status: RegionalLeaseReturnBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnBaselineStatus): Boolean {
        when (status) {
            RegionalLeaseReturnBaselineStatus.DRAFT -> {
                when (next) {
                    RegionalLeaseReturnBaselineStatus.PENDING, RegionalLeaseReturnBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalLeaseReturnBaselineStatus.PENDING -> return next == RegionalLeaseReturnBaselineStatus.ACTIVE || next == RegionalLeaseReturnBaselineStatus.CLOSED
            RegionalLeaseReturnBaselineStatus.ACTIVE -> return next == RegionalLeaseReturnBaselineStatus.SUSPENDED || next == RegionalLeaseReturnBaselineStatus.CLOSED
            RegionalLeaseReturnBaselineStatus.SUSPENDED -> return next == RegionalLeaseReturnBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnBaselineStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((fuelLiters - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalLeaseReturnBaseline {
        return RegionalLeaseReturnBaseline(
            code = code,
            name = name,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            reviewer = reviewer,
            floorValue = floorValue,
            cost = cost,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional lease return baseline in status draft. */
fun newRegionalLeaseReturnBaseline(code: String, name: String): RegionalLeaseReturnBaseline {
    return RegionalLeaseReturnBaseline(
        code = code,
        name = name,
        updatedAt = 0,
        shiftCode = "",
        reviewer = "",
        floorValue = 0.0,
        cost = 0.0,
        startsAt = 0,
        fuelLiters = 0.0,
        status = RegionalLeaseReturnBaselineStatus.DRAFT,
    )
}
