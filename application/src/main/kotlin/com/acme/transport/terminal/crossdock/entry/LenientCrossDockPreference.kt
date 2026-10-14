package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient cross dock preference is in its life cycle. */
enum class LenientCrossDockPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient cross dock preference and its current status. */
data class LenientCrossDockPreference(
    var code: String,
    var title: String,
    var label: String,
    var cost: Double,
    var weight: Double,
    var status: LenientCrossDockPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCrossDockPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCrossDockPreferenceStatus): Boolean {
        when (status) {
            LenientCrossDockPreferenceStatus.REQUESTED -> return next == LenientCrossDockPreferenceStatus.GRANTED
            LenientCrossDockPreferenceStatus.GRANTED -> return next == LenientCrossDockPreferenceStatus.REVOKED || next == LenientCrossDockPreferenceStatus.EXPIRED
            LenientCrossDockPreferenceStatus.REVOKED -> {
                when (next) {
                    LenientCrossDockPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCrossDockPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("lenient cross dock preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun netLoad(): Double = round((cost - weight) * 100) / 100

    fun snapshot(): LenientCrossDockPreference {
        return LenientCrossDockPreference(
            code = code,
            title = title,
            label = label,
            cost = cost,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient cross dock preference in status requested. */
fun newLenientCrossDockPreference(code: String, title: String): LenientCrossDockPreference {
    return LenientCrossDockPreference(
        code = code,
        title = title,
        label = "",
        cost = 0.0,
        weight = 0.0,
        status = LenientCrossDockPreferenceStatus.REQUESTED,
    )
}
