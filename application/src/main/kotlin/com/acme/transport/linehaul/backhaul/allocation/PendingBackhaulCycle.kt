package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.normalize

/** Where a pending backhaul cycle is in its life cycle. */
enum class PendingBackhaulCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending backhaul cycle and its current status. */
data class PendingBackhaulCycle(
    var code: String,
    var name: String,
    var locale: String,
    var vehicleId: String,
    var startsAt: Int,
    var unit: String,
    var externalRef: String,
    var status: PendingBackhaulCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingBackhaulCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingBackhaulCycleStatus): Boolean {
        when (status) {
            PendingBackhaulCycleStatus.REQUESTED -> {
                when (next) {
                    PendingBackhaulCycleStatus.REVOKED, PendingBackhaulCycleStatus.EXPIRED -> return true
                }
                return false
            }
            PendingBackhaulCycleStatus.GRANTED -> return next == PendingBackhaulCycleStatus.EXPIRED
            PendingBackhaulCycleStatus.REVOKED -> return next == PendingBackhaulCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingBackhaulCycleStatus) {
        if (!canMoveTo(next)) {
            error("pending backhaul cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): PendingBackhaulCycle {
        return PendingBackhaulCycle(
            code = code,
            name = name,
            locale = locale,
            vehicleId = vehicleId,
            startsAt = startsAt,
            unit = unit,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending backhaul cycle in status requested. */
fun newPendingBackhaulCycle(code: String, name: String): PendingBackhaulCycle {
    return PendingBackhaulCycle(
        code = code,
        name = name,
        locale = "",
        vehicleId = "",
        startsAt = 0,
        unit = "",
        externalRef = "",
        status = PendingBackhaulCycleStatus.REQUESTED,
    )
}
