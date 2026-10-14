package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive transit time preference records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a adaptive transit time preference is in its life cycle. */
enum class AdaptiveTransitTimePreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive transit time preference, with what the planners need to act on it. */
data class AdaptiveTransitTimePreference(
    var code: String,
    var label: String,
    var maximum: Double,
    var source: String,
    var loadKg: Double,
    var unit: String,
    var status: AdaptiveTransitTimePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTransitTimePreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTransitTimePreferenceStatus): Boolean {
        when (status) {
            AdaptiveTransitTimePreferenceStatus.NEW -> {
                when (next) {
                    AdaptiveTransitTimePreferenceStatus.DONE -> return true
                }
                return false
            }
            AdaptiveTransitTimePreferenceStatus.QUEUED -> return next == AdaptiveTransitTimePreferenceStatus.DONE || next == AdaptiveTransitTimePreferenceStatus.FAILED
            AdaptiveTransitTimePreferenceStatus.RUNNING -> {
                when (next) {
                    AdaptiveTransitTimePreferenceStatus.DONE -> return true
                }
                return false
            }
            AdaptiveTransitTimePreferenceStatus.DONE -> return next == AdaptiveTransitTimePreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveTransitTimePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive transit time preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round(maximum * loadKg * 100) / 100

    fun snapshot(): AdaptiveTransitTimePreference {
        return AdaptiveTransitTimePreference(
            code = code,
            label = label,
            maximum = maximum,
            source = source,
            loadKg = loadKg,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive transit time preference in status new. */
fun newAdaptiveTransitTimePreference(code: String, label: String): AdaptiveTransitTimePreference {
    return AdaptiveTransitTimePreference(
        code = code,
        label = label,
        maximum = 0.0,
        source = "",
        loadKg = 0.0,
        unit = "",
        status = AdaptiveTransitTimePreferenceStatus.NEW,
    )
}
