package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize

/** Where a legacy locker slot is in its life cycle. */
enum class LegacyLockerSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy locker slot and its current status. */
data class LegacyLockerSlot(
    var code: String,
    var label: String,
    var description: String,
    var cost: Double,
    var phone: String,
    var visible: Boolean,
    var status: LegacyLockerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLockerSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLockerSlotStatus): Boolean {
        when (status) {
            LegacyLockerSlotStatus.PLANNED -> return next == LegacyLockerSlotStatus.SCHEDULED || next == LegacyLockerSlotStatus.COMPLETED
            LegacyLockerSlotStatus.SCHEDULED -> return next == LegacyLockerSlotStatus.COMPLETED
            LegacyLockerSlotStatus.IN_PROGRESS -> return next == LegacyLockerSlotStatus.COMPLETED || next == LegacyLockerSlotStatus.CANCELLED
            LegacyLockerSlotStatus.COMPLETED -> return next == LegacyLockerSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyLockerSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy locker slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy locker slot"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): LegacyLockerSlot {
        return LegacyLockerSlot(
            code = code,
            label = label,
            description = description,
            cost = cost,
            phone = phone,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy locker slot in status planned. */
fun newLegacyLockerSlot(code: String, label: String): LegacyLockerSlot {
    return LegacyLockerSlot(
        code = code,
        label = label,
        description = "",
        cost = 0.0,
        phone = "",
        visible = false,
        status = LegacyLockerSlotStatus.PLANNED,
    )
}
