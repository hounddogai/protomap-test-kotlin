package com.acme.transport.customs.broker.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy broker window records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a legacy broker window is in its life cycle. */
enum class LegacyBrokerWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy broker window and its current status. */
data class LegacyBrokerWindow(
    var code: String,
    var name: String,
    var contactEmail: String,
    var rate: Double,
    var graceDays: Int,
    var fuelLiters: Double,
    var active: Boolean,
    var routeCode: String,
    var startsAt: Int,
    var status: LegacyBrokerWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBrokerWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBrokerWindowStatus): Boolean {
        when (status) {
            LegacyBrokerWindowStatus.PROPOSED -> {
                when (next) {
                    LegacyBrokerWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyBrokerWindowStatus.APPROVED -> return next == LegacyBrokerWindowStatus.REJECTED
            LegacyBrokerWindowStatus.REJECTED -> {
                when (next) {
                    LegacyBrokerWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBrokerWindowStatus) {
        if (!canMoveTo(next)) {
            error("legacy broker window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy broker window"} $code"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): LegacyBrokerWindow {
        return LegacyBrokerWindow(
            code = code,
            name = name,
            contactEmail = contactEmail,
            rate = rate,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            active = active,
            routeCode = routeCode,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy broker window in status proposed. */
fun newLegacyBrokerWindow(code: String, name: String): LegacyBrokerWindow {
    return LegacyBrokerWindow(
        code = code,
        name = name,
        contactEmail = "",
        rate = 0.0,
        graceDays = 0,
        fuelLiters = 0.0,
        active = false,
        routeCode = "",
        startsAt = 0,
        status = LegacyBrokerWindowStatus.PROPOSED,
    )
}
