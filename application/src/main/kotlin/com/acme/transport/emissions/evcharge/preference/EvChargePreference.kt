package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge preference records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a ev charge preference is in its life cycle. */
enum class EvChargePreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A ev charge preference as the ev charge team keeps it. */
data class EvChargePreference(
    var code: String,
    var title: String,
    var startsAt: Int,
    var visible: Boolean,
    var distanceKm: Double,
    var endsAt: Int,
    var etaMinutes: Int,
    var status: EvChargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargePreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargePreferenceStatus): Boolean {
        when (status) {
            EvChargePreferenceStatus.PROPOSED -> return next == EvChargePreferenceStatus.REJECTED || next == EvChargePreferenceStatus.WITHDRAWN
            EvChargePreferenceStatus.APPROVED -> {
                when (next) {
                    EvChargePreferenceStatus.REJECTED, EvChargePreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            EvChargePreferenceStatus.REJECTED -> {
                when (next) {
                    EvChargePreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("ev charge preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"ev charge preference"} $code"

    fun weightedExposure(): Double = round((startsAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): EvChargePreference {
        return EvChargePreference(
            code = code,
            title = title,
            startsAt = startsAt,
            visible = visible,
            distanceKm = distanceKm,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a ev charge preference in status proposed. */
fun newEvChargePreference(code: String, title: String): EvChargePreference {
    return EvChargePreference(
        code = code,
        title = title,
        startsAt = 0,
        visible = false,
        distanceKm = 0.0,
        endsAt = 0,
        etaMinutes = 0,
        status = EvChargePreferenceStatus.PROPOSED,
    )
}
