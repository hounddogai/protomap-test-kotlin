package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.text.normalize

/** Where a scheduled fuel surcharge cycle is in its life cycle. */
enum class ScheduledFuelSurchargeCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled fuel surcharge cycle and its current status. */
data class ScheduledFuelSurchargeCycle(
    var code: String,
    var label: String,
    var reviewer: String,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var status: ScheduledFuelSurchargeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelSurchargeCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelSurchargeCycleStatus): Boolean {
        when (status) {
            ScheduledFuelSurchargeCycleStatus.PROPOSED -> return next == ScheduledFuelSurchargeCycleStatus.APPROVED || next == ScheduledFuelSurchargeCycleStatus.REJECTED
            ScheduledFuelSurchargeCycleStatus.APPROVED -> return next == ScheduledFuelSurchargeCycleStatus.REJECTED || next == ScheduledFuelSurchargeCycleStatus.WITHDRAWN
            ScheduledFuelSurchargeCycleStatus.REJECTED -> {
                when (next) {
                    ScheduledFuelSurchargeCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledFuelSurchargeCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel surcharge cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): ScheduledFuelSurchargeCycle {
        return ScheduledFuelSurchargeCycle(
            code = code,
            label = label,
            reviewer = reviewer,
            unit = unit,
            attributes = attributes,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled fuel surcharge cycle in status proposed. */
fun newScheduledFuelSurchargeCycle(code: String, label: String): ScheduledFuelSurchargeCycle {
    return ScheduledFuelSurchargeCycle(
        code = code,
        label = label,
        reviewer = "",
        unit = "",
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        status = ScheduledFuelSurchargeCycleStatus.PROPOSED,
    )
}
