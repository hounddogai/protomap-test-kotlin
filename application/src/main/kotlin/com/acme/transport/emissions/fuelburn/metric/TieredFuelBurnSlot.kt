package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.normalize

/** Where a tiered fuel burn slot is in its life cycle. */
enum class TieredFuelBurnSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered fuel burn slot and its current status. */
data class TieredFuelBurnSlot(
    var code: String,
    var name: String,
    var dock: String,
    var checksum: String,
    var version: Int,
    var status: TieredFuelBurnSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelBurnSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelBurnSlotStatus): Boolean {
        when (status) {
            TieredFuelBurnSlotStatus.OPEN -> {
                when (next) {
                    TieredFuelBurnSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredFuelBurnSlotStatus.IN_REVIEW -> {
                when (next) {
                    TieredFuelBurnSlotStatus.RESOLVED -> return true
                }
                return false
            }
            TieredFuelBurnSlotStatus.RESOLVED -> {
                when (next) {
                    TieredFuelBurnSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredFuelBurnSlotStatus.REOPENED -> {
                when (next) {
                    TieredFuelBurnSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelBurnSlotStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel burn slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): TieredFuelBurnSlot {
        return TieredFuelBurnSlot(
            code = code,
            name = name,
            dock = dock,
            checksum = checksum,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered fuel burn slot in status open. */
fun newTieredFuelBurnSlot(code: String, name: String): TieredFuelBurnSlot {
    return TieredFuelBurnSlot(
        code = code,
        name = name,
        dock = "",
        checksum = "",
        version = 0,
        status = TieredFuelBurnSlotStatus.OPEN,
    )
}
