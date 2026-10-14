package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.text.normalize

// The strict tariff cycle records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a strict tariff cycle is in its life cycle. */
enum class StrictTariffCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict tariff cycle and its current status. */
data class StrictTariffCycle(
    var code: String,
    var title: String,
    var startsAt: Int,
    var notes: String,
    var visible: Boolean,
    var status: StrictTariffCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTariffCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTariffCycleStatus): Boolean {
        when (status) {
            StrictTariffCycleStatus.PLANNED -> {
                when (next) {
                    StrictTariffCycleStatus.SCHEDULED -> return true
                }
                return false
            }
            StrictTariffCycleStatus.SCHEDULED -> return next == StrictTariffCycleStatus.IN_PROGRESS || next == StrictTariffCycleStatus.CANCELLED
            StrictTariffCycleStatus.IN_PROGRESS -> {
                when (next) {
                    StrictTariffCycleStatus.COMPLETED -> return true
                }
                return false
            }
            StrictTariffCycleStatus.COMPLETED -> {
                when (next) {
                    StrictTariffCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTariffCycleStatus) {
        if (!canMoveTo(next)) {
            error("strict tariff cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): StrictTariffCycle {
        return StrictTariffCycle(
            code = code,
            title = title,
            startsAt = startsAt,
            notes = notes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict tariff cycle in status planned. */
fun newStrictTariffCycle(code: String, title: String): StrictTariffCycle {
    return StrictTariffCycle(
        code = code,
        title = title,
        startsAt = 0,
        notes = "",
        visible = false,
        status = StrictTariffCycleStatus.PLANNED,
    )
}
