package com.acme.transport.crewing.roster.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster allocation is in its life cycle. */
enum class RosterAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A roster allocation as the roster team keeps it. */
data class RosterAllocation(
    var code: String,
    var name: String,
    var quantity: Int,
    var batchSize: Int,
    var category: String,
    var timezone: String,
    var checksum: String,
    var floorValue: Double,
    var status: RosterAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterAllocationStatus): Boolean {
        when (status) {
            RosterAllocationStatus.DRAFT -> return next == RosterAllocationStatus.SUSPENDED
            RosterAllocationStatus.PENDING -> {
                when (next) {
                    RosterAllocationStatus.SUSPENDED, RosterAllocationStatus.CLOSED -> return true
                }
                return false
            }
            RosterAllocationStatus.ACTIVE -> {
                when (next) {
                    RosterAllocationStatus.CLOSED -> return true
                }
                return false
            }
            RosterAllocationStatus.SUSPENDED -> return next == RosterAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RosterAllocationStatus) {
        if (!canMoveTo(next)) {
            error("roster allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster allocation"} $code"

    fun adjustedExposure(): Double = round(floorValue * quantity.toDouble() * 100) / 100

    fun snapshot(): RosterAllocation {
        return RosterAllocation(
            code = code,
            name = name,
            quantity = quantity,
            batchSize = batchSize,
            category = category,
            timezone = timezone,
            checksum = checksum,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a roster allocation in status draft. */
fun newRosterAllocation(code: String, name: String): RosterAllocation {
    return RosterAllocation(
        code = code,
        name = name,
        quantity = 0,
        batchSize = 0,
        category = "",
        timezone = "",
        checksum = "",
        floorValue = 0.0,
        status = RosterAllocationStatus.DRAFT,
    )
}
