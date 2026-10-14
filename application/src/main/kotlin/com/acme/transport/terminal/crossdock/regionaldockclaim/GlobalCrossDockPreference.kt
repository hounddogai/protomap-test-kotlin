package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cross dock preference is in its life cycle. */
enum class GlobalCrossDockPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global cross dock preference and its current status. */
data class GlobalCrossDockPreference(
    var code: String,
    var label: String,
    var country: String,
    var segment: String,
    var fuelLiters: Double,
    var minimum: Double,
    var priority: Int,
    var ceiling: Double,
    var currency: String,
    var status: GlobalCrossDockPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCrossDockPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCrossDockPreferenceStatus): Boolean {
        when (status) {
            GlobalCrossDockPreferenceStatus.OPEN -> return next == GlobalCrossDockPreferenceStatus.IN_REVIEW
            GlobalCrossDockPreferenceStatus.IN_REVIEW -> {
                when (next) {
                    GlobalCrossDockPreferenceStatus.REOPENED -> return true
                }
                return false
            }
            GlobalCrossDockPreferenceStatus.RESOLVED -> return next == GlobalCrossDockPreferenceStatus.REOPENED
            GlobalCrossDockPreferenceStatus.REOPENED -> {
                when (next) {
                    GlobalCrossDockPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCrossDockPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("global cross dock preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((fuelLiters - minimum) * 100) / 100

    fun snapshot(): GlobalCrossDockPreference {
        return GlobalCrossDockPreference(
            code = code,
            label = label,
            country = country,
            segment = segment,
            fuelLiters = fuelLiters,
            minimum = minimum,
            priority = priority,
            ceiling = ceiling,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global cross dock preference in status open. */
fun newGlobalCrossDockPreference(code: String, label: String): GlobalCrossDockPreference {
    return GlobalCrossDockPreference(
        code = code,
        label = label,
        country = "",
        segment = "",
        fuelLiters = 0.0,
        minimum = 0.0,
        priority = 0,
        ceiling = 0.0,
        currency = "",
        status = GlobalCrossDockPreferenceStatus.OPEN,
    )
}
