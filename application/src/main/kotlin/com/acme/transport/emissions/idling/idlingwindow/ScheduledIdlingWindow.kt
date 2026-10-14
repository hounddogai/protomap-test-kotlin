package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled idling window records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a scheduled idling window is in its life cycle. */
enum class ScheduledIdlingWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled idling window. Its code is unique within a tenant. */
data class ScheduledIdlingWindow(
    var code: String,
    var name: String,
    var loadKg: Double,
    var visible: Boolean,
    var sequence: Int,
    var currency: String,
    var label: String,
    var status: ScheduledIdlingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledIdlingWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledIdlingWindowStatus): Boolean {
        when (status) {
            ScheduledIdlingWindowStatus.NEW -> {
                when (next) {
                    ScheduledIdlingWindowStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledIdlingWindowStatus.QUEUED -> return next == ScheduledIdlingWindowStatus.RUNNING || next == ScheduledIdlingWindowStatus.FAILED
            ScheduledIdlingWindowStatus.RUNNING -> return next == ScheduledIdlingWindowStatus.DONE || next == ScheduledIdlingWindowStatus.FAILED
            ScheduledIdlingWindowStatus.DONE -> {
                when (next) {
                    ScheduledIdlingWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledIdlingWindowStatus) {
        if (!canMoveTo(next)) {
            error("scheduled idling window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled idling window"} $code"

    fun projectedTotal(): Double = round(loadKg * sequence.toDouble() * 100) / 100

    fun snapshot(): ScheduledIdlingWindow {
        return ScheduledIdlingWindow(
            code = code,
            name = name,
            loadKg = loadKg,
            visible = visible,
            sequence = sequence,
            currency = currency,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled idling window in status new. */
fun newScheduledIdlingWindow(code: String, name: String): ScheduledIdlingWindow {
    return ScheduledIdlingWindow(
        code = code,
        name = name,
        loadKg = 0.0,
        visible = false,
        sequence = 0,
        currency = "",
        label = "",
        status = ScheduledIdlingWindowStatus.NEW,
    )
}
