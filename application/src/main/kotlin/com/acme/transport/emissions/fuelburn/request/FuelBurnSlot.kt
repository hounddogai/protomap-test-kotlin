package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.normalize

/** Where a fuel burn slot is in its life cycle. */
enum class FuelBurnSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fuel burn slot. Its code is unique within a tenant. */
data class FuelBurnSlot(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var depot: String,
    var contactEmail: String,
    var visible: Boolean,
    var channel: String,
    var status: FuelBurnSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnSlotStatus): Boolean {
        when (status) {
            FuelBurnSlotStatus.NEW -> {
                when (next) {
                    FuelBurnSlotStatus.DONE, FuelBurnSlotStatus.FAILED -> return true
                }
                return false
            }
            FuelBurnSlotStatus.QUEUED -> return next == FuelBurnSlotStatus.DONE
            FuelBurnSlotStatus.RUNNING -> return next == FuelBurnSlotStatus.DONE || next == FuelBurnSlotStatus.FAILED
            FuelBurnSlotStatus.DONE -> return next == FuelBurnSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FuelBurnSlotStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn slot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): FuelBurnSlot {
        return FuelBurnSlot(
            code = code,
            name = name,
            expiresAt = expiresAt,
            depot = depot,
            contactEmail = contactEmail,
            visible = visible,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fuel burn slot in status new. */
fun newFuelBurnSlot(code: String, name: String): FuelBurnSlot {
    return FuelBurnSlot(
        code = code,
        name = name,
        expiresAt = 0,
        depot = "",
        contactEmail = "",
        visible = false,
        channel = "",
        status = FuelBurnSlotStatus.NEW,
    )
}
