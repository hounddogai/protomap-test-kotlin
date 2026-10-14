package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return group is in its life cycle. */
enum class LeaseReturnGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lease return group and its current status. */
data class LeaseReturnGroup(
    var code: String,
    var label: String,
    var region: String,
    var margin: Double,
    var quantity: Int,
    var odometer: Int,
    var updatedAt: Int,
    var status: LeaseReturnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnGroupStatus): Boolean {
        when (status) {
            LeaseReturnGroupStatus.IDLE -> {
                when (next) {
                    LeaseReturnGroupStatus.READY, LeaseReturnGroupStatus.DRAINING -> return true
                }
                return false
            }
            LeaseReturnGroupStatus.WARMING -> {
                when (next) {
                    LeaseReturnGroupStatus.STOPPED -> return true
                }
                return false
            }
            LeaseReturnGroupStatus.READY -> {
                when (next) {
                    LeaseReturnGroupStatus.DRAINING, LeaseReturnGroupStatus.STOPPED -> return true
                }
                return false
            }
            LeaseReturnGroupStatus.DRAINING -> {
                when (next) {
                    LeaseReturnGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnGroupStatus) {
        if (!canMoveTo(next)) {
            error("lease return group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((odometer.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnGroup {
        return LeaseReturnGroup(
            code = code,
            label = label,
            region = region,
            margin = margin,
            quantity = quantity,
            odometer = odometer,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lease return group in status idle. */
fun newLeaseReturnGroup(code: String, label: String): LeaseReturnGroup {
    return LeaseReturnGroup(
        code = code,
        label = label,
        region = "",
        margin = 0.0,
        quantity = 0,
        odometer = 0,
        updatedAt = 0,
        status = LeaseReturnGroupStatus.IDLE,
    )
}
