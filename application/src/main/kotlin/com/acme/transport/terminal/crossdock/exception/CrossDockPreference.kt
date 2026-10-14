package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock preference is in its life cycle. */
enum class CrossDockPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock preference as the cross dock team keeps it. */
data class CrossDockPreference(
    var code: String,
    var title: String,
    var maximum: Double,
    var fuelLiters: Double,
    var minimum: Double,
    var description: String,
    var externalRef: String,
    var segment: String,
    var status: CrossDockPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockPreferenceStatus): Boolean {
        when (status) {
            CrossDockPreferenceStatus.IDLE -> return next == CrossDockPreferenceStatus.WARMING || next == CrossDockPreferenceStatus.STOPPED
            CrossDockPreferenceStatus.WARMING -> {
                when (next) {
                    CrossDockPreferenceStatus.READY -> return true
                }
                return false
            }
            CrossDockPreferenceStatus.READY -> return next == CrossDockPreferenceStatus.STOPPED
            CrossDockPreferenceStatus.DRAINING -> return next == CrossDockPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CrossDockPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("cross dock preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cross dock preference"} $code"

    fun weightedValue(): Double = round(minimum * maximum * 100) / 100

    fun snapshot(): CrossDockPreference {
        return CrossDockPreference(
            code = code,
            title = title,
            maximum = maximum,
            fuelLiters = fuelLiters,
            minimum = minimum,
            description = description,
            externalRef = externalRef,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a cross dock preference in status idle. */
fun newCrossDockPreference(code: String, title: String): CrossDockPreference {
    return CrossDockPreference(
        code = code,
        title = title,
        maximum = 0.0,
        fuelLiters = 0.0,
        minimum = 0.0,
        description = "",
        externalRef = "",
        segment = "",
        status = CrossDockPreferenceStatus.IDLE,
    )
}
