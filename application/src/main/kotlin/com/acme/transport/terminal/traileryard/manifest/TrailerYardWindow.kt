package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard window is in its life cycle. */
enum class TrailerYardWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a trailer yard window and its current status. */
data class TrailerYardWindow(
    var code: String,
    var label: String,
    var name: String,
    var reviewer: String,
    var driverId: String,
    var unit: String,
    var durationMinutes: Int,
    var amount: Double,
    var odometer: Int,
    var stopCount: Int,
    var status: TrailerYardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardWindowStatus): Boolean {
        when (status) {
            TrailerYardWindowStatus.IDLE -> {
                when (next) {
                    TrailerYardWindowStatus.STOPPED -> return true
                }
                return false
            }
            TrailerYardWindowStatus.WARMING -> {
                when (next) {
                    TrailerYardWindowStatus.READY, TrailerYardWindowStatus.DRAINING -> return true
                }
                return false
            }
            TrailerYardWindowStatus.READY -> return next == TrailerYardWindowStatus.DRAINING || next == TrailerYardWindowStatus.STOPPED
            TrailerYardWindowStatus.DRAINING -> return next == TrailerYardWindowStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerYardWindowStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"trailer yard window"} $code"

    fun weightedExposure(): Double = round(stopCount.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): TrailerYardWindow {
        return TrailerYardWindow(
            code = code,
            label = label,
            name = name,
            reviewer = reviewer,
            driverId = driverId,
            unit = unit,
            durationMinutes = durationMinutes,
            amount = amount,
            odometer = odometer,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a trailer yard window in status idle. */
fun newTrailerYardWindow(code: String, label: String): TrailerYardWindow {
    return TrailerYardWindow(
        code = code,
        label = label,
        name = "",
        reviewer = "",
        driverId = "",
        unit = "",
        durationMinutes = 0,
        amount = 0.0,
        odometer = 0,
        stopCount = 0,
        status = TrailerYardWindowStatus.IDLE,
    )
}
