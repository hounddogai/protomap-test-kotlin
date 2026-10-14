package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize

/** Where a hold slot is in its life cycle. */
enum class HoldSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A hold slot. Its code is unique within a tenant. */
data class HoldSlot(
    var code: String,
    var name: String,
    var timezone: String,
    var revision: Int,
    var reviewer: String,
    var status: HoldSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldSlotStatus): Boolean {
        when (status) {
            HoldSlotStatus.PLANNED -> return next == HoldSlotStatus.IN_PROGRESS || next == HoldSlotStatus.COMPLETED
            HoldSlotStatus.SCHEDULED -> {
                when (next) {
                    HoldSlotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            HoldSlotStatus.IN_PROGRESS -> {
                when (next) {
                    HoldSlotStatus.CANCELLED -> return true
                }
                return false
            }
            HoldSlotStatus.COMPLETED -> return next == HoldSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HoldSlotStatus) {
        if (!canMoveTo(next)) {
            error("hold slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): HoldSlot {
        return HoldSlot(
            code = code,
            name = name,
            timezone = timezone,
            revision = revision,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold slot in status planned. */
fun newHoldSlot(code: String, name: String): HoldSlot {
    return HoldSlot(
        code = code,
        name = name,
        timezone = "",
        revision = 0,
        reviewer = "",
        status = HoldSlotStatus.PLANNED,
    )
}
