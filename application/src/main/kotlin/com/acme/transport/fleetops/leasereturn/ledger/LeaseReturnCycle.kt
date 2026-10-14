package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return cycle is in its life cycle. */
enum class LeaseReturnCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lease return cycle as the lease return team keeps it. */
data class LeaseReturnCycle(
    var code: String,
    var label: String,
    var graceDays: Int,
    var expiresAt: Int,
    var rate: Double,
    var unit: String,
    var limit: Int,
    var ceiling: Double,
    var currency: String,
    var checksum: String,
    var active: Boolean,
    var status: LeaseReturnCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnCycleStatus): Boolean {
        when (status) {
            LeaseReturnCycleStatus.PLANNED -> {
                when (next) {
                    LeaseReturnCycleStatus.SCHEDULED, LeaseReturnCycleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LeaseReturnCycleStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnCycleStatus.IN_PROGRESS, LeaseReturnCycleStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnCycleStatus.IN_PROGRESS -> return next == LeaseReturnCycleStatus.COMPLETED
            LeaseReturnCycleStatus.COMPLETED -> {
                when (next) {
                    LeaseReturnCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnCycleStatus) {
        if (!canMoveTo(next)) {
            error("lease return cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((rate - ceiling) * 100) / 100

    fun snapshot(): LeaseReturnCycle {
        return LeaseReturnCycle(
            code = code,
            label = label,
            graceDays = graceDays,
            expiresAt = expiresAt,
            rate = rate,
            unit = unit,
            limit = limit,
            ceiling = ceiling,
            currency = currency,
            checksum = checksum,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lease return cycle in status planned. */
fun newLeaseReturnCycle(code: String, label: String): LeaseReturnCycle {
    return LeaseReturnCycle(
        code = code,
        label = label,
        graceDays = 0,
        expiresAt = 0,
        rate = 0.0,
        unit = "",
        limit = 0,
        ceiling = 0.0,
        currency = "",
        checksum = "",
        active = false,
        status = LeaseReturnCycleStatus.PLANNED,
    )
}
