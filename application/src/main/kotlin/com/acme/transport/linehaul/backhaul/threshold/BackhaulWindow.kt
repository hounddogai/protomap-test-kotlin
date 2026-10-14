package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul window is in its life cycle. */
enum class BackhaulWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One backhaul window, with what the planners need to act on it. */
data class BackhaulWindow(
    var code: String,
    var title: String,
    var quantity: Int,
    var priority: Int,
    var cost: Double,
    var fuelLiters: Double,
    var vehicleId: String,
    var revision: Int,
    var maximum: Double,
    var margin: Double,
    var status: BackhaulWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulWindowStatus): Boolean {
        when (status) {
            BackhaulWindowStatus.PROPOSED -> {
                when (next) {
                    BackhaulWindowStatus.APPROVED -> return true
                }
                return false
            }
            BackhaulWindowStatus.APPROVED -> return next == BackhaulWindowStatus.WITHDRAWN
            BackhaulWindowStatus.REJECTED -> return next == BackhaulWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BackhaulWindowStatus) {
        if (!canMoveTo(next)) {
            error("backhaul window $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((maximum - margin) * 100) / 100

    fun snapshot(): BackhaulWindow {
        return BackhaulWindow(
            code = code,
            title = title,
            quantity = quantity,
            priority = priority,
            cost = cost,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            revision = revision,
            maximum = maximum,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a backhaul window in status proposed. */
fun newBackhaulWindow(code: String, title: String): BackhaulWindow {
    return BackhaulWindow(
        code = code,
        title = title,
        quantity = 0,
        priority = 0,
        cost = 0.0,
        fuelLiters = 0.0,
        vehicleId = "",
        revision = 0,
        maximum = 0.0,
        margin = 0.0,
        status = BackhaulWindowStatus.PROPOSED,
    )
}
