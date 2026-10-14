package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.normalize

/** Where a strict dock door tier is in its life cycle. */
enum class StrictDockDoorTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict dock door tier. Its code is unique within a tenant. */
data class StrictDockDoorTier(
    var code: String,
    var label: String,
    var category: String,
    var dock: String,
    var vehicleId: String,
    var attempts: Int,
    var status: StrictDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorTierStatus): Boolean {
        when (status) {
            StrictDockDoorTierStatus.REQUESTED -> {
                when (next) {
                    StrictDockDoorTierStatus.GRANTED, StrictDockDoorTierStatus.EXPIRED -> return true
                }
                return false
            }
            StrictDockDoorTierStatus.GRANTED -> {
                when (next) {
                    StrictDockDoorTierStatus.REVOKED -> return true
                }
                return false
            }
            StrictDockDoorTierStatus.REVOKED -> return next == StrictDockDoorTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): StrictDockDoorTier {
        return StrictDockDoorTier(
            code = code,
            label = label,
            category = category,
            dock = dock,
            vehicleId = vehicleId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict dock door tier in status requested. */
fun newStrictDockDoorTier(code: String, label: String): StrictDockDoorTier {
    return StrictDockDoorTier(
        code = code,
        label = label,
        category = "",
        dock = "",
        vehicleId = "",
        attempts = 0,
        status = StrictDockDoorTierStatus.REQUESTED,
    )
}
