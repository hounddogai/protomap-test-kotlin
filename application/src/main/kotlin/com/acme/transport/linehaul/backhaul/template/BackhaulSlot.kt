package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul slot is in its life cycle. */
enum class BackhaulSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One backhaul slot, with what the planners need to act on it. */
data class BackhaulSlot(
    var code: String,
    var name: String,
    var amount: Double,
    var durationMinutes: Int,
    var graceDays: Int,
    var status: BackhaulSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulSlotStatus): Boolean {
        when (status) {
            BackhaulSlotStatus.REQUESTED -> {
                when (next) {
                    BackhaulSlotStatus.GRANTED, BackhaulSlotStatus.EXPIRED -> return true
                }
                return false
            }
            BackhaulSlotStatus.GRANTED -> return next == BackhaulSlotStatus.REVOKED || next == BackhaulSlotStatus.EXPIRED
            BackhaulSlotStatus.REVOKED -> return next == BackhaulSlotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BackhaulSlotStatus) {
        if (!canMoveTo(next)) {
            error("backhaul slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((amount + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): BackhaulSlot {
        return BackhaulSlot(
            code = code,
            name = name,
            amount = amount,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul slot in status requested. */
fun newBackhaulSlot(code: String, name: String): BackhaulSlot {
    return BackhaulSlot(
        code = code,
        name = name,
        amount = 0.0,
        durationMinutes = 0,
        graceDays = 0,
        status = BackhaulSlotStatus.REQUESTED,
    )
}
