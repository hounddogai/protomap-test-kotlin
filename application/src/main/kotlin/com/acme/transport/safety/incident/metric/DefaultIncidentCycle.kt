package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default incident cycle is in its life cycle. */
enum class DefaultIncidentCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default incident cycle and its current status. */
data class DefaultIncidentCycle(
    var code: String,
    var name: String,
    var score: Double,
    var floorValue: Double,
    var odometer: Int,
    var ceiling: Double,
    var status: DefaultIncidentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIncidentCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIncidentCycleStatus): Boolean {
        when (status) {
            DefaultIncidentCycleStatus.NEW -> return next == DefaultIncidentCycleStatus.QUEUED || next == DefaultIncidentCycleStatus.FAILED
            DefaultIncidentCycleStatus.QUEUED -> {
                when (next) {
                    DefaultIncidentCycleStatus.RUNNING, DefaultIncidentCycleStatus.DONE -> return true
                }
                return false
            }
            DefaultIncidentCycleStatus.RUNNING -> {
                when (next) {
                    DefaultIncidentCycleStatus.FAILED -> return true
                }
                return false
            }
            DefaultIncidentCycleStatus.DONE -> return next == DefaultIncidentCycleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultIncidentCycleStatus) {
        if (!canMoveTo(next)) {
            error("default incident cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(odometer.toDouble() * score * 100) / 100

    fun snapshot(): DefaultIncidentCycle {
        return DefaultIncidentCycle(
            code = code,
            name = name,
            score = score,
            floorValue = floorValue,
            odometer = odometer,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default incident cycle in status new. */
fun newDefaultIncidentCycle(code: String, name: String): DefaultIncidentCycle {
    return DefaultIncidentCycle(
        code = code,
        name = name,
        score = 0.0,
        floorValue = 0.0,
        odometer = 0,
        ceiling = 0.0,
        status = DefaultIncidentCycleStatus.NEW,
    )
}
