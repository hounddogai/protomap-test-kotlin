package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending tire slot records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a pending tire slot is in its life cycle. */
enum class PendingTireSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending tire slot and its current status. */
data class PendingTireSlot(
    var code: String,
    var name: String,
    var active: Boolean,
    var priority: Int,
    var channel: String,
    var description: String,
    var createdAt: Int,
    var unit: String,
    var status: PendingTireSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTireSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTireSlotStatus): Boolean {
        when (status) {
            PendingTireSlotStatus.PROPOSED -> {
                when (next) {
                    PendingTireSlotStatus.REJECTED, PendingTireSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTireSlotStatus.APPROVED -> return next == PendingTireSlotStatus.REJECTED || next == PendingTireSlotStatus.WITHDRAWN
            PendingTireSlotStatus.REJECTED -> {
                when (next) {
                    PendingTireSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTireSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending tire slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round(priority.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): PendingTireSlot {
        return PendingTireSlot(
            code = code,
            name = name,
            active = active,
            priority = priority,
            channel = channel,
            description = description,
            createdAt = createdAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending tire slot in status proposed. */
fun newPendingTireSlot(code: String, name: String): PendingTireSlot {
    return PendingTireSlot(
        code = code,
        name = name,
        active = false,
        priority = 0,
        channel = "",
        description = "",
        createdAt = 0,
        unit = "",
        status = PendingTireSlotStatus.PROPOSED,
    )
}
