package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.text.normalize

/** Where a dwell window is in its life cycle. */
enum class DwellWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell window, with what the planners need to act on it. */
data class DwellWindow(
    var code: String,
    var label: String,
    var retries: Int,
    var shiftCode: String,
    var unit: String,
    var status: DwellWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellWindowStatus): Boolean {
        when (status) {
            DwellWindowStatus.DRAFT -> return next == DwellWindowStatus.ACTIVE || next == DwellWindowStatus.CLOSED
            DwellWindowStatus.PENDING -> return next == DwellWindowStatus.ACTIVE || next == DwellWindowStatus.SUSPENDED
            DwellWindowStatus.ACTIVE -> return next == DwellWindowStatus.SUSPENDED || next == DwellWindowStatus.CLOSED
            DwellWindowStatus.SUSPENDED -> return next == DwellWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DwellWindowStatus) {
        if (!canMoveTo(next)) {
            error("dwell window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): DwellWindow {
        return DwellWindow(
            code = code,
            label = label,
            retries = retries,
            shiftCode = shiftCode,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a dwell window in status draft. */
fun newDwellWindow(code: String, label: String): DwellWindow {
    return DwellWindow(
        code = code,
        label = label,
        retries = 0,
        shiftCode = "",
        unit = "",
        status = DwellWindowStatus.DRAFT,
    )
}
