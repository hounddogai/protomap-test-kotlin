package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock window is in its life cycle. */
enum class CrossDockWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a cross dock window and its current status. */
data class CrossDockWindow(
    var code: String,
    var label: String,
    var currency: String,
    var quantity: Int,
    var lane: String,
    var maximum: Double,
    var dock: String,
    var odometer: Int,
    var status: CrossDockWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockWindowStatus): Boolean {
        when (status) {
            CrossDockWindowStatus.IDLE -> return next == CrossDockWindowStatus.READY || next == CrossDockWindowStatus.DRAINING
            CrossDockWindowStatus.WARMING -> return next == CrossDockWindowStatus.READY
            CrossDockWindowStatus.READY -> return next == CrossDockWindowStatus.STOPPED
            CrossDockWindowStatus.DRAINING -> {
                when (next) {
                    CrossDockWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockWindowStatus) {
        if (!canMoveTo(next)) {
            error("cross dock window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cross dock window"} $code"

    fun effectiveTotal(): Double = round(odometer.toDouble() * maximum * 100) / 100

    fun snapshot(): CrossDockWindow {
        return CrossDockWindow(
            code = code,
            label = label,
            currency = currency,
            quantity = quantity,
            lane = lane,
            maximum = maximum,
            dock = dock,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a cross dock window in status idle. */
fun newCrossDockWindow(code: String, label: String): CrossDockWindow {
    return CrossDockWindow(
        code = code,
        label = label,
        currency = "",
        quantity = 0,
        lane = "",
        maximum = 0.0,
        dock = "",
        odometer = 0,
        status = CrossDockWindowStatus.IDLE,
    )
}
