package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel burn override is in its life cycle. */
enum class RollingFuelBurnOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling fuel burn override, with what the planners need to act on it. */
data class RollingFuelBurnOverride(
    var code: String,
    var name: String,
    var odometer: Int,
    var minimum: Double,
    var phone: String,
    var active: Boolean,
    var dock: String,
    var attempts: Int,
    var stopCount: Int,
    var status: RollingFuelBurnOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnOverrideStatus): Boolean {
        when (status) {
            RollingFuelBurnOverrideStatus.PROPOSED -> {
                when (next) {
                    RollingFuelBurnOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingFuelBurnOverrideStatus.APPROVED -> return next == RollingFuelBurnOverrideStatus.REJECTED || next == RollingFuelBurnOverrideStatus.WITHDRAWN
            RollingFuelBurnOverrideStatus.REJECTED -> return next == RollingFuelBurnOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnOverrideStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((attempts.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): RollingFuelBurnOverride {
        return RollingFuelBurnOverride(
            code = code,
            name = name,
            odometer = odometer,
            minimum = minimum,
            phone = phone,
            active = active,
            dock = dock,
            attempts = attempts,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling fuel burn override in status proposed. */
fun newRollingFuelBurnOverride(code: String, name: String): RollingFuelBurnOverride {
    return RollingFuelBurnOverride(
        code = code,
        name = name,
        odometer = 0,
        minimum = 0.0,
        phone = "",
        active = false,
        dock = "",
        attempts = 0,
        stopCount = 0,
        status = RollingFuelBurnOverrideStatus.PROPOSED,
    )
}
