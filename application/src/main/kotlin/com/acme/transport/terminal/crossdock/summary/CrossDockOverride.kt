package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock override is in its life cycle. */
enum class CrossDockOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One cross dock override, with what the planners need to act on it. */
data class CrossDockOverride(
    var code: String,
    var label: String,
    var visible: Boolean,
    var capacity: Int,
    var weight: Double,
    var amount: Double,
    var odometer: Int,
    var status: CrossDockOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockOverrideStatus): Boolean {
        when (status) {
            CrossDockOverrideStatus.DRAFT -> {
                when (next) {
                    CrossDockOverrideStatus.PENDING -> return true
                }
                return false
            }
            CrossDockOverrideStatus.PENDING -> {
                when (next) {
                    CrossDockOverrideStatus.ACTIVE -> return true
                }
                return false
            }
            CrossDockOverrideStatus.ACTIVE -> return next == CrossDockOverrideStatus.CLOSED
            CrossDockOverrideStatus.SUSPENDED -> return next == CrossDockOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CrossDockOverrideStatus) {
        if (!canMoveTo(next)) {
            error("cross dock override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round((weight - amount) * 100) / 100

    fun snapshot(): CrossDockOverride {
        return CrossDockOverride(
            code = code,
            label = label,
            visible = visible,
            capacity = capacity,
            weight = weight,
            amount = amount,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock override in status draft. */
fun newCrossDockOverride(code: String, label: String): CrossDockOverride {
    return CrossDockOverride(
        code = code,
        label = label,
        visible = false,
        capacity = 0,
        weight = 0.0,
        amount = 0.0,
        odometer = 0,
        status = CrossDockOverrideStatus.DRAFT,
    )
}
