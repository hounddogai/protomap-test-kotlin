package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default dwell preference records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a default dwell preference is in its life cycle. */
enum class DefaultDwellPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default dwell preference. Its code is unique within a tenant. */
data class DefaultDwellPreference(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var endsAt: Int,
    var windowDays: Int,
    var pinned: Boolean,
    var currency: String,
    var floorValue: Double,
    var graceDays: Int,
    var cost: Double,
    var minimum: Double,
    var status: DefaultDwellPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDwellPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDwellPreferenceStatus): Boolean {
        when (status) {
            DefaultDwellPreferenceStatus.REQUESTED -> {
                when (next) {
                    DefaultDwellPreferenceStatus.GRANTED, DefaultDwellPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultDwellPreferenceStatus.GRANTED -> return next == DefaultDwellPreferenceStatus.REVOKED || next == DefaultDwellPreferenceStatus.EXPIRED
            DefaultDwellPreferenceStatus.REVOKED -> {
                when (next) {
                    DefaultDwellPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDwellPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default dwell preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((windowDays.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultDwellPreference {
        return DefaultDwellPreference(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            endsAt = endsAt,
            windowDays = windowDays,
            pinned = pinned,
            currency = currency,
            floorValue = floorValue,
            graceDays = graceDays,
            cost = cost,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default dwell preference in status requested. */
fun newDefaultDwellPreference(code: String, name: String): DefaultDwellPreference {
    return DefaultDwellPreference(
        code = code,
        name = name,
        fuelLiters = 0.0,
        endsAt = 0,
        windowDays = 0,
        pinned = false,
        currency = "",
        floorValue = 0.0,
        graceDays = 0,
        cost = 0.0,
        minimum = 0.0,
        status = DefaultDwellPreferenceStatus.REQUESTED,
    )
}
