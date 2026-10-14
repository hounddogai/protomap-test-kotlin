package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hold window is in its life cycle. */
enum class ManualHoldWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual hold window, with what the planners need to act on it. */
data class ManualHoldWindow(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var odometer: Int,
    var country: String,
    var quantity: Int,
    var attempts: Int,
    var startsAt: Int,
    var channel: String,
    var maximum: Double,
    var routeCode: String,
    var status: ManualHoldWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHoldWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHoldWindowStatus): Boolean {
        when (status) {
            ManualHoldWindowStatus.PROPOSED -> return next == ManualHoldWindowStatus.APPROVED || next == ManualHoldWindowStatus.WITHDRAWN
            ManualHoldWindowStatus.APPROVED -> {
                when (next) {
                    ManualHoldWindowStatus.REJECTED, ManualHoldWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualHoldWindowStatus.REJECTED -> return next == ManualHoldWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualHoldWindowStatus) {
        if (!canMoveTo(next)) {
            error("manual hold window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual hold window"} $code"

    fun projectedExposure(): Double = round((attempts.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): ManualHoldWindow {
        return ManualHoldWindow(
            code = code,
            name = name,
            pinned = pinned,
            odometer = odometer,
            country = country,
            quantity = quantity,
            attempts = attempts,
            startsAt = startsAt,
            channel = channel,
            maximum = maximum,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual hold window in status proposed. */
fun newManualHoldWindow(code: String, name: String): ManualHoldWindow {
    return ManualHoldWindow(
        code = code,
        name = name,
        pinned = false,
        odometer = 0,
        country = "",
        quantity = 0,
        attempts = 0,
        startsAt = 0,
        channel = "",
        maximum = 0.0,
        routeCode = "",
        status = ManualHoldWindowStatus.PROPOSED,
    )
}
